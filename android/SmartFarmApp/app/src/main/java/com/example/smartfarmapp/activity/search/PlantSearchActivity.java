package com.example.smartfarmapp.activity.search;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfarmapp.R;
import com.example.smartfarmapp.adapter.PlantAdapter;
import com.example.smartfarmapp.ai.TFLiteHelper;
import com.example.smartfarmapp.model.CustomDictionary;
import com.example.smartfarmapp.model.PlantResponse;
import com.example.smartfarmapp.network.ApiClient;
import com.example.smartfarmapp.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlantSearchActivity extends AppCompatActivity {

    private static final String TAG = "PlantSearch_Log";
    private EditText etSearch;
    private ImageButton btnHome, btnAiSearch;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private List<PlantResponse> plantList = new ArrayList<>();
    private ApiService apiService;
    private TFLiteHelper aiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_search);

        etSearch = findViewById(R.id.etSearchPlant);
        recyclerView = findViewById(R.id.recyclerViewPlant);
        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnAiSearch = findViewById(R.id.btn_ai_search);

        // 1. API 및 리사이클러뷰 초기화
        apiService = ApiClient.getClient().create(ApiService.class);
        adapter = new PlantAdapter(plantList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        aiHelper = new TFLiteHelper();
        aiHelper.init(this);

        // 2. 버튼 클릭 리스너
        btnHome.setOnClickListener(v -> finish());

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            loadCombinedData(keyword);
        });

        btnAiSearch.setOnClickListener(v -> showAiOptionDialog());

        // 3. 초기 권한 체크 및 데이터 로드
        checkCameraPermission();
        loadCombinedData(""); // 처음엔 전체 로드
    }

    // 🌟 [RDA API + 내 사전] 데이터를 통합해서 불러오는 핵심 메서드
    private void loadCombinedData(String keyword) {
        plantList.clear(); // 새로운 검색 시작 시 리스트 비움
        Log.d(TAG, "데이터 로딩 시작 (검색어: " + keyword + ")");

        // A. 먼저 공공 API(RDA) 데이터를 가져옵니다.
        Call<List<PlantResponse>> rdaCall = keyword.isEmpty() ? apiService.getAllPlants() : apiService.searchPlant(keyword);

        rdaCall.enqueue(new Callback<List<PlantResponse>>() {
            @Override
            public void onResponse(Call<List<PlantResponse>> call, Response<List<PlantResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    plantList.addAll(response.body());
                    Log.d(TAG, "RDA 수신 성공: " + response.body().size());
                }
                // RDA 성공 여부와 상관없이 무조건 내 사전을 이어서 가져옵니다.
                loadMyCustomPlants(keyword);
            }

            @Override
            public void onFailure(Call<List<PlantResponse>> call, Throwable t) {
                Log.e(TAG, "RDA 통신 에러: " + t.getMessage());
                loadMyCustomPlants(keyword); // 에러 나도 내 사전은 띄움
            }
        });
    }

    // 🌟 내 사전(CustomDictionary) 데이터를 가져와 plantList에 합치는 메서드
    private void loadMyCustomPlants(String keyword) {
        apiService.searchCustomDictionary(keyword).enqueue(new Callback<List<CustomDictionary>>() {
            @Override
            public void onResponse(Call<List<CustomDictionary>> call, Response<List<CustomDictionary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (CustomDictionary item : response.body()) {
                        plantList.add(new PlantResponse(
                                item.getName(),
                                "내 사전 식물", // 설명 칸에 표시
                                "상세 정보 확인",
                                item.getImageUrl(),
                                "정보 없음", "정보 없음", "정보 없음"
                        ));
                    }
                    Log.d(TAG, "내 사전 합치기 완료. 현재 총 개수: " + plantList.size());
                }
                // 🌟 모든 데이터가 plantList에 담겼을 때 마지막에 한 번만 새로고침!
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<CustomDictionary>> call, Throwable t) {
                Log.e(TAG, "내 사전 통신 에러: " + t.getMessage());
                adapter.notifyDataSetChanged();
            }
        });
    }

    // --- 카메라/AI 로직 (생략된 부분 유지) ---
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void showAiOptionDialog() {
        CharSequence[] options = {"📷 카메라 촬영", "🖼️ 앨범 선택"};
        new AlertDialog.Builder(this).setTitle("식물 찾기").setItems(options, (dialog, which) -> {
            if (which == 0) cameraLauncher.launch(null);
            else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            }
        }).show();
    }

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) processImageWithAI(bitmap);
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Uri uri = result.getData().getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        processImageWithAI(bitmap);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
    );

    private void processImageWithAI(Bitmap bitmap) {
        String plantName = aiHelper.classifyPlant(bitmap);
        if (!plantName.equals("알 수 없음")) {
            etSearch.setText(plantName);
            loadCombinedData(plantName);
        }
    }
}
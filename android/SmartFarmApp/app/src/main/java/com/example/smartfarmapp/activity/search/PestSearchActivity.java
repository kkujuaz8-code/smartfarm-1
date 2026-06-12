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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfarmapp.R;
import com.example.smartfarmapp.adapter.PestAdapter;
import com.example.smartfarmapp.ai.TFLiteHelper;
import com.example.smartfarmapp.model.PestDictionary;
import com.example.smartfarmapp.model.PestResponse;
import com.example.smartfarmapp.network.ApiClient;
import com.example.smartfarmapp.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PestSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnHome;
    private Button btnSearch;
    private ImageButton btnAiSearch; // 🌟 핵심 버튼
    private RecyclerView recyclerView;
    private PestAdapter adapter;
    private List<PestResponse> pestList = new ArrayList<>();
    private ApiService apiService;
    private TFLiteHelper aiHelper;

    // 카메라/앨범 결과 처리 런처
    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> { if (bitmap != null) processImageWithAI(bitmap); }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Uri imageUri = result.getData().getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        processImageWithAI(bitmap);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
    );

    private void processImageWithAI(Bitmap bitmap) {
        try {
            String pestName = aiHelper.classifyDisease(bitmap);
            if (!pestName.equals("알 수 없음") && !pestName.equals("인식 불가")) {
                etSearch.setText(pestName);
                searchPestsCombined(pestName); // 인식 즉시 검색
            } else {
                Toast.makeText(this, "인식 실패: " + pestName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pest_search);

        // 1. 뷰 초기화
        etSearch = findViewById(R.id.etSearchPest);
        recyclerView = findViewById(R.id.recyclerViewPest);
        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnAiSearch = findViewById(R.id.btn_ai_search); // 🌟 ID가 XML과 맞는지 꼭 확인!

        // 2. 리사이클러뷰 세팅
        apiService = ApiClient.getClient().create(ApiService.class);
        adapter = new PestAdapter(pestList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 3. AI 초기화
        aiHelper = new TFLiteHelper();
        aiHelper.init(this);

        // 4. 클릭 리스너 연결
        btnHome.setOnClickListener(v -> finish());

        // 🌟 버튼 클릭 이벤트 (다이얼로그 띄우기)
        btnAiSearch.setOnClickListener(v -> {
            Log.d("PestSearch", "AI 버튼 클릭됨"); // 클릭 확인용 로그
            CharSequence[] options = {"📷 카메라 촬영", "🖼️ 앨범에서 선택"};
            new AlertDialog.Builder(this)
                    .setTitle("병해충 촬영/선택")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) cameraLauncher.launch(null);
                        else {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            galleryLauncher.launch(intent);
                        }
                    })
                    .show();
        });

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) loadAllPests();
            else searchPestsCombined(keyword);
        });

        // 5. 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        loadAllPests();
    }

    // 서버 통신 로직 (기존 성공 코드 유지)
    private void loadAllPests() {
        apiService.getAllPests().enqueue(new Callback<List<PestResponse>>() {
            @Override
            public void onResponse(Call<List<PestResponse>> call, Response<List<PestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pestList.clear();
                    pestList.addAll(response.body());
                    loadCustomPests();
                }
            }
            @Override public void onFailure(Call<List<PestResponse>> call, Throwable t) { loadCustomPests(); }
        });
    }

    private void loadCustomPests() {
        apiService.searchCustomPest("").enqueue(new Callback<List<PestDictionary>>() {
            @Override
            public void onResponse(Call<List<PestDictionary>> call, Response<List<PestDictionary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (PestDictionary item : response.body()) {
                        pestList.add(new PestResponse(item.getName(), "커스텀 도감", "정보 확인", item.getImageUrl()));
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<PestDictionary>> call, Throwable t) { adapter.notifyDataSetChanged(); }
        });
    }

    private void searchPestsCombined(String keyword) {
        pestList.clear();
        apiService.searchPest(keyword).enqueue(new Callback<List<PestResponse>>() {
            @Override
            public void onResponse(Call<List<PestResponse>> call, Response<List<PestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pestList.addAll(response.body());
                }
                searchCustom(keyword);
            }
            @Override public void onFailure(Call<List<PestResponse>> call, Throwable t) { searchCustom(keyword); }
        });
    }

    private void searchCustom(String keyword) {
        apiService.searchCustomPest(keyword).enqueue(new Callback<List<PestDictionary>>() {
            @Override
            public void onResponse(Call<List<PestDictionary>> call, Response<List<PestDictionary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (PestDictionary item : response.body()) {
                        pestList.add(new PestResponse(item.getName(), "커스텀 도감", "정보 확인", item.getImageUrl()));
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<PestDictionary>> call, Throwable t) { adapter.notifyDataSetChanged(); }
        });
    }
}
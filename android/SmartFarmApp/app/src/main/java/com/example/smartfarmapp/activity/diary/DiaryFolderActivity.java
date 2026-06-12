package com.example.smartfarmapp.activity.diary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfarmapp.network.ApiClient;   // 🌟 추가
import com.example.smartfarmapp.service.ApiService;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.adapter.FolderAdapter;
import com.example.smartfarmapp.model.DiaryFolder;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody; // 삭제 응답 처리를 위해 필수
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryFolderActivity extends AppCompatActivity {

    private RecyclerView rvFolders;
    private ExtendedFloatingActionButton btnAddFolder;
    private ImageButton btnBack;

    private FolderAdapter adapter;
    private List<DiaryFolder> folderList = new ArrayList<>();
    private ApiService apiService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_folder);

        // 1. 사용자 ID 가져오기
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "testUser");

        // 2. 뷰 연결
        rvFolders = findViewById(R.id.rvFolders);
        btnAddFolder = findViewById(R.id.btnAddFolder);
        btnBack = findViewById(R.id.btnBack);

        // 3. 서버 연결 (ApiClient 한 줄로 통일!)
        apiService = ApiClient.getClient().create(ApiService.class);

        // 4. 리사이클러뷰 설정 (2열 격자 모양)
        rvFolders.setLayoutManager(new GridLayoutManager(this, 2));

        // 어댑터 연결 (클릭 시 이동, 롱클릭 시 삭제)
        adapter = new FolderAdapter(this, folderList, new FolderAdapter.OnFolderActionListener() {
            @Override
            public void onDeleteRequest(Long folderId, String folderName) {
                showDeleteDialog(folderId, folderName);
            }
        });
        rvFolders.setAdapter(adapter);

        // 5. 버튼 이벤트
        btnBack.setOnClickListener(v -> finish());
        btnAddFolder.setOnClickListener(v -> showAddDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면에 들어올 때마다 폴더 목록 새로고침
        loadFolders();
    }

    //  폴더 목록 불러오기
    private void loadFolders() {
        apiService.getMyFolders(userId).enqueue(new Callback<List<DiaryFolder>>() {
            @Override
            public void onResponse(Call<List<DiaryFolder>> call, Response<List<DiaryFolder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    folderList.clear();
                    folderList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DiaryFolderActivity.this, "폴더 목록 로딩 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DiaryFolder>> call, Throwable t) {
                Toast.makeText(DiaryFolderActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ➕ 폴더 추가 다이얼로그
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("새 일기장 만들기");

        final EditText input = new EditText(this);
        input.setHint("예: 방울토마토, 상추 등");
        builder.setView(input);

        builder.setPositiveButton("만들기", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                createFolder(name);
            } else {
                Toast.makeText(DiaryFolderActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    // 폴더 생성 요청
    private void createFolder(String name) {
        DiaryFolder newFolder = new DiaryFolder(name, userId);
        apiService.createFolder(newFolder).enqueue(new Callback<DiaryFolder>() {
            @Override
            public void onResponse(Call<DiaryFolder> call, Response<DiaryFolder> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DiaryFolderActivity.this, "일기장이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    loadFolders(); // 목록 갱신
                } else {
                    Toast.makeText(DiaryFolderActivity.this, "생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DiaryFolder> call, Throwable t) {
                Toast.makeText(DiaryFolderActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🗑️ 폴더 삭제 다이얼로그
    private void showDeleteDialog(Long folderId, String folderName) {
        new AlertDialog.Builder(this)
                .setTitle("일기장 삭제")
                .setMessage("[" + folderName + "] 을(를) 삭제하시겠습니까?\n⚠️ 안에 있는 일기도 모두 삭제됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> deleteFolder(folderId))
                .setNegativeButton("취소", null)
                .show();
    }

    // 폴더 삭제 요청
    private void deleteFolder(Long folderId) {
        apiService.deleteFolder(folderId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DiaryFolderActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                    loadFolders();
                } else {
                    Toast.makeText(DiaryFolderActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DiaryFolderActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
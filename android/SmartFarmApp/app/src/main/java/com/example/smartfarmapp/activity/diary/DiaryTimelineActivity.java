package com.example.smartfarmapp.activity.diary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfarmapp.network.ApiClient;   // 🌟 추가
import com.example.smartfarmapp.service.ApiService;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.adapter.DiaryTimelineAdapter;
import com.example.smartfarmapp.model.DiaryEntry;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryTimelineActivity extends AppCompatActivity {

    // 화면 요소 변수 선언
    private RecyclerView rvTimeline;
    private DiaryTimelineAdapter adapter;
    private List<DiaryEntry> diaryList = new ArrayList<>();

    private ImageButton btnBack;
    private FloatingActionButton btnWrite;
    private TextView tvTitle;

    // 데이터 변수
    private Long currentFolderId;
    private String currentFolderName;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_timeline);

        // 1. Intent로 전달받은 폴더 정보 꺼내기
        currentFolderId = getIntent().getLongExtra("folderId", -1);
        currentFolderName = getIntent().getStringExtra("folderName");

        // 2. View 연결
        btnBack = findViewById(R.id.btnBack);
        btnWrite = findViewById(R.id.btnWrite);
        tvTitle = findViewById(R.id.tvTitle);
        rvTimeline = findViewById(R.id.recyclerView);

        // 3. 제목 설정
        if (currentFolderName != null && tvTitle != null) {
            tvTitle.setText(currentFolderName + " 기록");
        }

        // 4. 리사이클러뷰 초기화
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiaryTimelineAdapter(this, diaryList);
        rvTimeline.setAdapter(adapter);

        // 5. 서버 연결 (ApiClient 한 줄로 통일!)
        apiService = ApiClient.getClient().create(ApiService.class);

        // 6. 버튼 이벤트 리스너 설정
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnWrite != null) {
            btnWrite.setOnClickListener(v -> {
                Intent intent = new Intent(DiaryTimelineActivity.this, DiaryActivity.class);
                intent.putExtra("folderId", currentFolderId);
                startActivity(intent);
            });
        }
    }

    // 화면이 다시 보일 때마다 데이터를 새로 불러옵니다
    @Override
    protected void onResume() {
        super.onResume();
        loadTimelineData();
    }

    // 서버에서 일기 목록 가져오기
    private void loadTimelineData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = pref.getString("userId", "testUser");

        apiService.getDiaries(userId, currentFolderId).enqueue(new Callback<List<DiaryEntry>>() {
            @Override
            public void onResponse(Call<List<DiaryEntry>> call, Response<List<DiaryEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    diaryList.clear();
                    diaryList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DiaryTimelineActivity.this, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DiaryEntry>> call, Throwable t) {
                Toast.makeText(DiaryTimelineActivity.this, "서버 통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
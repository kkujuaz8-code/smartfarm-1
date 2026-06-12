package com.example.smartfarmapp.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout; // ⚠️ 버튼 타입이 LinearLayout임
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfarmapp.activity.search.PestSearchActivity;
import com.example.smartfarmapp.activity.search.PlantSearchActivity;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.activity.sensor.SensorActivity;
import com.example.smartfarmapp.activity.diary.DiaryFolderActivity;
import com.example.smartfarmapp.activity.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    // 버튼들이 LinearLayout으로 되어 있습니다.
    private LinearLayout btnSmartFarm, btnPlantSearch, btnDiary, btnPestSearch;
    private TextView btnLogout, tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 뷰 연결
        btnSmartFarm = findViewById(R.id.btnSmartFarm);
        btnPlantSearch = findViewById(R.id.btnPlantSearch);
        btnDiary = findViewById(R.id.btnDiary);
        btnPestSearch = findViewById(R.id.btnPestSearch);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        // 사용자 이름 표시
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "사용자");
        tvWelcome.setText(userId + "님\n환영합니다!");

        // 2. 클릭 이벤트 연결

        // (1) 🌡 스마트팜 센서 화면
        btnSmartFarm.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            startActivity(intent);
        });

        // (2) 🌿 식물 도감 화면
        btnPlantSearch.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, PlantSearchActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // 파일이 없으면 네이버 검색 (예외처리 유지)
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://search.naver.com/search.naver?query=식물도감"));
                startActivity(intent);
            }
        });

        // (3) 📔 성장 일기 화면 (🌟 여기가 핵심 수정!)
        btnDiary.setOnClickListener(v -> {
            // 폴더 목록으로 이동
            Intent intent = new Intent(MainActivity.this, DiaryFolderActivity.class);
            startActivity(intent);
        });

        // (4) 🐛 병해충 검색 화면
        btnPestSearch.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, PestSearchActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // 파일이 없으면 구글 검색 (예외처리 유지)
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?tbm=isch&q=식물+병해충"));
                startActivity(intent);
            }
        });

        // (5) 로그아웃
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
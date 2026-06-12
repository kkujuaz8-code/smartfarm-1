package com.example.smartfarmapp.activity.search;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.smartfarmapp.R;

public class DetailActivity extends AppCompatActivity {

    private ImageView ivDetail;
    private TextView tvName, tvDescription, tvWater, tvRepot, tvSunlight, tvSolution;
    private LinearLayout layoutPlantInfo, layoutPestInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. 뷰 초기화
        ivDetail = findViewById(R.id.ivDetail);
        tvName = findViewById(R.id.tvDetailName);
        tvDescription = findViewById(R.id.tvDetailDesc);
        tvWater = findViewById(R.id.tvDetailWater);
        tvRepot = findViewById(R.id.tvDetailRepot);
        tvSunlight = findViewById(R.id.tvDetailSunlight);
        tvSolution = findViewById(R.id.tvDetailSolution);
        layoutPlantInfo = findViewById(R.id.layoutPlantSpecific);
        layoutPestInfo = findViewById(R.id.layoutPestSpecific);
        ImageButton btnBack = findViewById(R.id.btnDetailBack);

        btnBack.setOnClickListener(v -> finish());

        // 2. Intent 데이터 수신
        String name = getIntent().getStringExtra("name");
        String desc = getIntent().getStringExtra("desc");
        String imgUrl = getIntent().getStringExtra("imgUrl");
        String water = getIntent().getStringExtra("water");
        String repot = getIntent().getStringExtra("repot");
        String sunlight = getIntent().getStringExtra("sunlight");
        String solution = getIntent().getStringExtra("solution");

        // 3. 기본 텍스트 설정
        tvName.setText(name != null ? name : "이름 없음");
        tvDescription.setText(desc != null ? desc : "설명이 없습니다.");

        // 🌟 4. 이미지 URL 전처리 (농사로 API의 복수 URL 처리)
        if (imgUrl != null && !imgUrl.isEmpty()) {
            // 여러 개의 URL이 '|'로 묶여 있을 경우 첫 번째만 사용
            if (imgUrl.contains("|")) {
                imgUrl = imgUrl.split("\\|")[0];
            }
            // 보안 차단 방지를 위한 https 변환
            if (imgUrl.startsWith("http://")) {
                imgUrl = imgUrl.replace("http://", "https://");
            }

            Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivDetail);
        } else {
            ivDetail.setImageResource(R.drawable.ic_launcher_background);
        }

        // 🌟 5. 데이터 종류(식물 vs 병해충)에 따른 화면 구성
        // 식물 데이터인 경우 (water, repot, sunlight 중 하나라도 존재)
        if (water != null || repot != null || sunlight != null) {
            layoutPlantInfo.setVisibility(View.VISIBLE);
            layoutPestInfo.setVisibility(View.GONE);

            tvWater.setText("💧 물 주기: " + (water != null ? water : "정보 없음"));
            tvRepot.setText("🪴 분갈이: " + (repot != null ? repot : "정보 없음"));
            tvSunlight.setText("☀️ 햇빛: " + (sunlight != null ? sunlight : "정보 없음"));
        }
        // 병해충 데이터인 경우 (solution이 존재)
        else if (solution != null && !solution.isEmpty()) {
            layoutPlantInfo.setVisibility(View.GONE);
            layoutPestInfo.setVisibility(View.VISIBLE);
            tvSolution.setText(solution);
        }
        // 둘 다 없는 기본 상태 (필요 시 처리)
        else {
            layoutPlantInfo.setVisibility(View.GONE);
            layoutPestInfo.setVisibility(View.GONE);
        }
    }
}
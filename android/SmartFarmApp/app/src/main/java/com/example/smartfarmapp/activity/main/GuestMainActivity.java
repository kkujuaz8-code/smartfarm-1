package com.example.smartfarmapp.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfarmapp.activity.search.PestSearchActivity;
import com.example.smartfarmapp.activity.search.PlantSearchActivity;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.activity.login.LoginActivity;

public class GuestMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_main); // 아까 만든 xml 파일

        Button btnPlant = findViewById(R.id.btnGuestPlant);
        Button btnPest = findViewById(R.id.btnGuestPest);
        Button btnLogin = findViewById(R.id.btnGoLogin);

        btnPlant.setOnClickListener(v -> startActivity(new Intent(this, PlantSearchActivity.class)));
        btnPest.setOnClickListener(v -> startActivity(new Intent(this, PestSearchActivity.class)));

        // 로그인하러 가기
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
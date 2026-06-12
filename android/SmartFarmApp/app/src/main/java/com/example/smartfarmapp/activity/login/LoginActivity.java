package com.example.smartfarmapp.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfarmapp.network.ApiClient;
import com.example.smartfarmapp.service.ApiService;
import com.example.smartfarmapp.activity.main.GuestMainActivity;
import com.example.smartfarmapp.activity.main.MainActivity;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.model.LoginRequest;
import com.example.smartfarmapp.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPw;
    private Button btnLogin;
    private TextView tvJoin, tvGuest;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.etId);
        etPw = findViewById(R.id.etPw);
        btnLogin = findViewById(R.id.btnLogin);
        tvJoin = findViewById(R.id.tvJoin);
        tvGuest = findViewById(R.id.tvGuest);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> {
            String userId = etId.getText().toString().trim();
            String userPw = etPw.getText().toString().trim();

            if (userId.isEmpty() || userPw.isEmpty()) {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            requestLogin(userId, userPw);
        });

        tvJoin.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, JoinActivity.class)));

        tvGuest.setOnClickListener(v -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                    .putString("userId", "Guest").apply();
            Toast.makeText(LoginActivity.this, "비회원으로 입장합니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, GuestMainActivity.class));
            finish();
        });
    }

    private void requestLogin(String userId, String password) {
        LoginRequest request = new LoginRequest(userId, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse result = response.body();
                    if (result.isSuccess()) {
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                                .putString("userId", userId).apply();
                        Toast.makeText(LoginActivity.this, "환영합니다, " + userId + "님!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 실패: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginError", t.getMessage() != null ? t.getMessage() : "Unknown error");
                Toast.makeText(LoginActivity.this, "네트워크 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
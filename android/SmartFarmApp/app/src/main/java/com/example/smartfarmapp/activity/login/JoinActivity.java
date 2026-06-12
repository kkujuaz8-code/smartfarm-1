package com.example.smartfarmapp.activity.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfarmapp.network.ApiClient;
import com.example.smartfarmapp.service.ApiService;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.model.LoginRequest;
import com.example.smartfarmapp.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends AppCompatActivity {

    private EditText etId, etPw, etName, etEmail;
    private Button btnJoinComplete, btnBack, btnCheckId;

    private ApiService apiService;
    private boolean isIdChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        etId = findViewById(R.id.etRegisterId);
        etPw = findViewById(R.id.etRegisterPw);
        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        btnJoinComplete = findViewById(R.id.btnRegisterComplete);
        btnBack = findViewById(R.id.btnBack);
        btnCheckId = findViewById(R.id.btnCheckId);

        // 🌟 ApiClient 한 줄로 통합
        apiService = ApiClient.getClient().create(ApiService.class);

        btnBack.setOnClickListener(v -> finish());

        // 아이디 중복 확인 버튼
        btnCheckId.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            if (id.isEmpty()) {
                Toast.makeText(this, "아이디를 먼저 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            apiService.checkId(id).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse result = response.body();
                        if (result.isSuccess()) {
                            isIdChecked = true;
                            Toast.makeText(JoinActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            isIdChecked = false;
                            Toast.makeText(JoinActivity.this, "중복된 아이디가 존재하여 회원가입이 불가합니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(JoinActivity.this, "중복 확인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(JoinActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnJoinComplete.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String pw = etPw.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (id.isEmpty() || pw.isEmpty() || name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isIdChecked) {
                Toast.makeText(this, "아이디 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(id, pw, name, email);
            apiService.register(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse result = response.body();
                        if (result.isSuccess()) {
                            Toast.makeText(JoinActivity.this, "가입 성공! 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(JoinActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(JoinActivity.this, "가입 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(JoinActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
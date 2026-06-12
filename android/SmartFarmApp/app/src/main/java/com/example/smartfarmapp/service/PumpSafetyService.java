package com.example.smartfarmapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.smartfarmapp.network.ApiClient;
import com.example.smartfarmapp.service.ApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PumpSafetyService extends Service {

    private ApiService apiService;
    private Long currentDeviceId = 1L;

    @Override
    public void onCreate() {
        super.onCreate();
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            currentDeviceId = intent.getLongExtra("deviceId", 1L);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // 🌟 앱이 최근 앱 목록에서 스와이프되어 강제 종료될 때 실행됩니다.
        Log.d("PumpSafetyService", "🚨 앱 강제 종료 감지! 펌프 정지 명령을 전송합니다.");

        // 💡 [수정됨] 세 번째 인자로 "SYSTEM_SAFETY"를 추가하여 서버 로그에 기록되게 합니다.
        apiService.requestPump(currentDeviceId, "OFF", "SYSTEM_SAFETY").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("PumpSafetyService", "✅ 안전 종료 명령 전송 성공");
                stopSelf();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("PumpSafetyService", "❌ 안전 종료 명령 전송 실패: " + t.getMessage());
                stopSelf();
            }
        });

        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
package com.example.smartfarmapp.network;

import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 🌟 서버 IP가 바뀌면 여기 딱 한 줄만 수정하면 앱 전체에 적용됩니다!
    // ⚠️ 에뮬레이터는 10.0.2.2, 실제 폰이나 학교에서는 192.168... 로 변경하세요. 123
    public static final String BASE_URL = "http://10.0.2.2:8585/";

    private static Retrofit retrofit = null;

    // 싱글톤 패턴 (메모리를 아끼기 위해 딱 한 번만 만듦)
    public static Retrofit getClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                    .build();
        }
        return retrofit;
    }
}
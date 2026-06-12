package com.example.smartfarmapp.service; // 패키지 경로는 프로젝트에 맞게 확인하세요

import com.example.smartfarmapp.model.DiaryEntry;
import com.example.smartfarmapp.model.DiaryFolder;
import com.example.smartfarmapp.model.LoginRequest;
import com.example.smartfarmapp.model.LoginResponse;
import com.example.smartfarmapp.model.PestResponse;
import com.example.smartfarmapp.model.PlantResponse;
import com.example.smartfarmapp.model.SensorData;
import com.example.smartfarmapp.model.CustomDictionary;
import com.example.smartfarmapp.model.PestDictionary;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ==========================================
    // 1. 회원 관리 (로그인, 회원가입)
    // ==========================================
    @GET("/api/member/check-id")
    Call<LoginResponse> checkId(@Query("userId") String userId);

    @POST("/api/member/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/member/join")
    Call<LoginResponse> register(@Body LoginRequest request);


    // ==========================================
    // 2. 식물일기
    // ==========================================
    @POST("/api/diary/save")
    Call<ResponseBody> saveDiary(@Body DiaryEntry entry);

    @GET("/api/diary/list")
    Call<List<DiaryEntry>> getDiaries(
            @Query("userId") String userId,
            @Query("folderId") Long folderId
    );

    @DELETE("/api/diary/delete/{id}")
    Call<ResponseBody> deleteDiary(@Path("id") Long id);


    // ==========================================
    // 3. 폴더 관리 (목록, 생성, 삭제)
    // ==========================================
    @GET("/api/folders")
    Call<List<DiaryFolder>> getMyFolders(@Query("userId") String userId);

    @POST("/api/folders")
    Call<DiaryFolder> createFolder(@Body DiaryFolder folder);

    @DELETE("/api/folders/{id}")
    Call<ResponseBody> deleteFolder(@Path("id") Long folderId);


    // ==========================================
    // 4. 센서 데이터 및 제어 (차트, 물주기)
    // ==========================================
    @GET("/sensor/chart")
    Call<List<SensorData>> getSensorChart(
            @Query("type") String type,
            @Query("userId") String userId,
            @Query("deviceId") Long deviceId
    );

    // 🌟 [수정됨] 서버 로그 기록을 위해 userId 파라미터가 추가되었습니다.
    @POST("device/pump")
    Call<ResponseBody> requestPump(
            @Query("deviceId") Long deviceId,
            @Query("state") String state,
            @Query("userId") String userId
    );


    // ==========================================
    // 5. 식물/병해충 도감 (전체 조회, 검색)
    // ==========================================

    // 식물 도감 (RDA)
    @GET("api/plants")
    Call<List<PlantResponse>> getAllPlants();

    @GET("api/plants/search")
    Call<List<PlantResponse>> searchPlant(@Query("name") String name);

    // 병해충 도감 (RDA)
    @GET("api/pests")
    Call<List<PestResponse>> getAllPests();

    @GET("api/pests/search")
    Call<List<PestResponse>> searchPest(@Query("keyword") String keyword);

    // 식물 커스텀 도감
    @GET("/api/plants/dictionary/search")
    Call<List<CustomDictionary>> searchCustomDictionary(@Query("keyword") String keyword);

    // 병해충 커스텀 도감
    @GET("/api/pests/custom")
    Call<List<PestDictionary>> searchCustomPest(@Query("keyword") String keyword);
}
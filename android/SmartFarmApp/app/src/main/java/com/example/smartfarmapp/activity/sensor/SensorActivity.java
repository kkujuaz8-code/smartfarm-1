package com.example.smartfarmapp.activity.sensor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfarmapp.network.ApiClient;

import com.example.smartfarmapp.R;
import com.example.smartfarmapp.model.SensorData;
import com.example.smartfarmapp.service.ApiService;
import com.example.smartfarmapp.service.PumpSafetyService;
import android.os.Handler;
import android.os.Looper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorActivity extends AppCompatActivity {

    private Long selectedDeviceId = 1L;   // 기기 선택용 (기본값: 1번 기기)

    // 화면 요소
    private LineChart chartTemp, chartHumid, chartSoil;
    private TextView tvRealTemp, tvRealHumid, tvRealSoil;
    private TextView tvAvgTemp, tvAvgHumid, tvAvgSoil;
    private RadioGroup radioGroup;
    private boolean isPumpOn = false;

    private ApiService apiService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // 1. 뷰 연결
        tvRealTemp = findViewById(R.id.tvRealTemp);
        tvRealHumid = findViewById(R.id.tvRealHumid);
        tvRealSoil = findViewById(R.id.tvRealSoil);
        tvAvgTemp = findViewById(R.id.tvAvgTemp);
        tvAvgHumid = findViewById(R.id.tvAvgHumid);
        tvAvgSoil = findViewById(R.id.tvAvgSoil);

        chartTemp = findViewById(R.id.chartTemp);
        chartHumid = findViewById(R.id.chartHumid);
        chartSoil = findViewById(R.id.chartSoil);

        radioGroup = findViewById(R.id.radioGroup);

        // 2. 데이터 및 서비스 초기화
        currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", "Guest");

        apiService = ApiClient.getClient().create(ApiService.class);

        // 3. 기기 선택 Spinner 설정
        setupDeviceSpinner();

        // 4. 상단 뒤로가기 버튼
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 5. 원격 물주기(펌프) 버튼 설정
        Button btnWater = findViewById(R.id.btnWater);
        btnWater.setOnClickListener(v -> {
            String command = isPumpOn ? "OFF" : "ON";
            controlPump(command, btnWater);
        });

        // 6. 차트 기본 스타일 설정
        setupChartStyle(chartTemp);
        setupChartStyle(chartHumid);
        setupChartStyle(chartSoil);

        // 7. 기간 선택(시간/일/월) 버튼 리스너
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String type = "hour";
            if (checkedId == R.id.btnDay) type = "day";
            else if (checkedId == R.id.btnMonth) type = "month";
            fetchSensorData(type);
        });
    }

    // 🌟 수정된 펌프 제어 메서드
    private void controlPump(String command, Button btnWater) {
        apiService.requestPump(selectedDeviceId, command, currentUserId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isPumpOn = command.equals("ON");

                    // UI 업데이트 (버튼 텍스트 및 색상 변경)
                    btnWater.setText(isPumpOn ? "물주기 중지" : "원격 물주기 실행");
                    btnWater.setBackgroundColor(isPumpOn ? Color.RED : Color.parseColor("#2196F3"));

                    Intent serviceIntent = new Intent(SensorActivity.this, PumpSafetyService.class);
                    serviceIntent.putExtra("deviceId", selectedDeviceId);

                    if (isPumpOn) {
                        startService(serviceIntent); // 펌프 켜면 안전 감시 시작

                        // 💡 핵심 추가 로직: 15초 뒤에 버튼 UI 원상복구
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 15초가 지났을 때 현재 UI 상태가 여전히 켜져있다면(isPumpOn == true) 강제 원상복구
                                if (isPumpOn) {
                                    isPumpOn = false;
                                    btnWater.setText("원격 물주기 실행");
                                    btnWater.setBackgroundColor(Color.parseColor("#2196F3"));
                                }
                            }
                        }, 15000); // 15000ms = 15초

                    } else {
                        stopService(serviceIntent);  // 사용자가 직접 펌프를 끄면 감시 종료
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(SensorActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPumpOn) {
            Button btnWater = findViewById(R.id.btnWater);
            controlPump("OFF", btnWater);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPumpOn) {
            Button btnWater = findViewById(R.id.btnWater);
            controlPump("OFF", btnWater);
        }
    }

    private void setupDeviceSpinner() {
        Spinner spinnerDevice = findViewById(R.id.spinnerDevice);

        String[] deviceNames = {"1번 기기", "2번 기기", "3번 기기", "4번 기기"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevice.setAdapter(adapter);

        spinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDeviceId = (long) (position + 1);

                String currentType = "hour";
                int checkedId = radioGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.btnDay) currentType = "day";
                else if (checkedId == R.id.btnMonth) currentType = "month";

                fetchSensorData(currentType);
                Toast.makeText(SensorActivity.this, selectedDeviceId + "번 기기 데이터 로드", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchSensorData(String type) {
        apiService.getSensorChart(type, currentUserId, selectedDeviceId)
                .enqueue(new Callback<List<SensorData>>() {
                    @Override
                    public void onResponse(Call<List<SensorData>> call, Response<List<SensorData>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<SensorData> dataList = response.body();

                            SensorData latest = dataList.get(dataList.size() - 1);
                            double latestTemp = latest.getTemperature();
                            double latestHumid = latest.getHumidity();
                            double latestSoil = latest.getSoilMoisture();

                            tvRealTemp.setText(String.format(Locale.getDefault(), "%.1f°", latestTemp));
                            tvRealHumid.setText(String.format(Locale.getDefault(), "%.1f%%", latestHumid));

                            // 💡==========================================================💡
                            // 추가된 토양수분 상태 판별 로직 (표 기준 반영)
                            String soilStatus;
                            if (latestSoil >= 800) {
                                soilStatus = "매우 건조";
                            } else if (latestSoil >= 600) {
                                soilStatus = "건조";
                            } else if (latestSoil >= 400) {
                                soilStatus = "적당";
                            } else if (latestSoil >= 200) {
                                soilStatus = "습윤";
                            } else {
                                soilStatus = "매우 습함";
                            }

                            // 텍스트뷰에 "적당(455%)" 형태로 출력
                            tvRealSoil.setText(String.format(Locale.getDefault(), "%s(%.0f%%)", soilStatus, latestSoil));
                            // 💡==========================================================💡

                            double sumTemp = 0, sumHumid = 0, sumSoil = 0;
                            for (SensorData d : dataList) {
                                sumTemp += d.getTemperature();
                                sumHumid += d.getHumidity();
                                sumSoil += d.getSoilMoisture();
                            }
                            int count = dataList.size();
                            tvAvgTemp.setText(String.format(Locale.getDefault(), "평균: %.1f°", sumTemp / count));
                            tvAvgHumid.setText(String.format(Locale.getDefault(), "평균: %.1f%%", sumHumid / count));
                            tvAvgSoil.setText(String.format(Locale.getDefault(), "평균: %.1f%%", sumSoil / count));

                            updateAllCharts(dataList, type);

                        } else {
                            tvRealTemp.setText("--.-°");
                            tvRealHumid.setText("--.-%");
                            tvRealSoil.setText("--.-%");
                            tvAvgTemp.setText("평균: --.-°");
                            chartTemp.clear(); chartHumid.clear(); chartSoil.clear();
                            Toast.makeText(SensorActivity.this, selectedDeviceId + "번 기기의 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SensorData>> call, Throwable t) {
                        Log.e("SensorActivity", "통신 에러: " + t.getMessage());
                        Toast.makeText(SensorActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAllCharts(List<SensorData> dataList, String type) {
        ArrayList<Entry> entriesTemp = new ArrayList<>();
        ArrayList<Entry> entriesHumid = new ArrayList<>();
        ArrayList<Entry> entriesSoil = new ArrayList<>();
        final ArrayList<String> xLabels = new ArrayList<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat fmtHour = new SimpleDateFormat("MM/dd HH시", Locale.getDefault());
        SimpleDateFormat fmtDay = new SimpleDateFormat("yy.MM.dd", Locale.getDefault());
        SimpleDateFormat fmtMonth = new SimpleDateFormat("yy.MM", Locale.getDefault());

        for (int i = 0; i < dataList.size(); i++) {
            SensorData d = dataList.get(i);

            entriesTemp.add(new Entry(i, (float) d.getTemperature()));
            entriesHumid.add(new Entry(i, (float) d.getHumidity()));
            entriesSoil.add(new Entry(i, (float) d.getSoilMoisture()));

            String label = d.getTimestamp();
            try {
                if (label != null && label.length() > 19) label = label.substring(0, 19);
                Date date = inputFormat.parse(label);

                if ("hour".equals(type)) label = fmtHour.format(date);
                else if ("day".equals(type)) label = fmtDay.format(date);
                else if ("month".equals(type)) label = fmtMonth.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            xLabels.add(label);
        }

        setChartData(chartTemp, entriesTemp, "온도", Color.parseColor("#FF5252"), xLabels);
        setChartData(chartHumid, entriesHumid, "습도", Color.parseColor("#2196F3"), xLabels);
        setChartData(chartSoil, entriesSoil, "토양수분", Color.parseColor("#4CAF50"), xLabels);
    }

    private void setChartData(LineChart chart, ArrayList<Entry> entries, String label, int color, ArrayList<String> xLabels) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(40);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
        chart.setVisibleXRangeMaximum(6);
        chart.moveViewToX(entries.size());
        chart.invalidate();
    }

    private void setupChartStyle(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-25);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
    }
}
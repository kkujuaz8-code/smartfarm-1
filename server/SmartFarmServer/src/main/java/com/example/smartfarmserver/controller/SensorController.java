package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.entity.SensorData;
import com.example.smartfarmserver.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SensorController {

    @Autowired
    private SensorRepository sensorRepository;

    @GetMapping("/sensor/chart")
    public List<SensorData> getSensorChart(
            @RequestParam("type") String type,
            @RequestParam("userId") String userId,
            @RequestParam("deviceId") Long deviceId) {

        // 1. DB 조회 (deviceId를 포함하여 정확한 기기의 데이터를 가져옴)
        List<SensorData> rawData = sensorRepository.findByUserIdAndDeviceIdOrderByTimestampAsc(userId, deviceId);

        // 데이터가 없거나 '시간별(hour)' 요청이면 가공 없이 바로 반환
        if (rawData.isEmpty() || "hour".equals(type)) {
            return rawData;
        }

        // 2. 그룹화 (날짜별 또는 월별로 묶기)
        Map<String, List<SensorData>> groupedMap = new LinkedHashMap<>();
        for (SensorData data : rawData) {
            String timeStr = data.getTimestamp().toString();
            String key = "";
            
            if ("day".equals(type) && timeStr.length() >= 10) {
                key = timeStr.substring(0, 10); // yyyy-MM-dd
            } else if ("month".equals(type) && timeStr.length() >= 7) {
                key = timeStr.substring(0, 7);  // yyyy-MM
            }

            if (!key.isEmpty()) {
                groupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(data);
            }
        }

        // 3. 평균 계산 (그래프가 너무 촘촘해지는 것을 방지)
        List<SensorData> averagedList = new ArrayList<>();
        for (String key : groupedMap.keySet()) {
            List<SensorData> group = groupedMap.get(key);
            double sumTemp = 0, sumHumid = 0, sumSoil = 0;

            for (SensorData d : group) {
                if (d.getTemperature() != null) sumTemp += d.getTemperature();
                if (d.getHumidity() != null) sumHumid += d.getHumidity();
                if (d.getSoilMoisture() != null) sumSoil += d.getSoilMoisture();
            }

            int size = group.size();
            SensorData avg = new SensorData();
            avg.setUserId(userId);
            avg.setDeviceId(deviceId); // 기기 ID 유지
            avg.setTemperature(Math.round(sumTemp / size * 10) / 10.0);
            avg.setHumidity(Math.round(sumHumid / size * 10) / 10.0);
            avg.setSoilMoisture(Math.round(sumSoil / size * 10) / 10.0);

            // 타임스탬프를 해당 날짜/월의 시작 시간으로 설정
            if (key.length() == 10) avg.setTimestamp(LocalDate.parse(key).atStartOfDay());
            else if (key.length() == 7) avg.setTimestamp(LocalDate.parse(key + "-01").atStartOfDay());

            averagedList.add(avg);
        }
        return averagedList;
    }
}
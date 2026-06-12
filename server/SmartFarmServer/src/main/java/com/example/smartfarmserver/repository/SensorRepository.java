package com.example.smartfarmserver.repository;

import com.example.smartfarmserver.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorRepository extends JpaRepository<SensorData, Long> {

    List<SensorData> findByUserIdOrderByTimestampAsc(String userId);

    // 🌟 이 줄이 핵심! device_id도 조건으로 추가
    List<SensorData> findByUserIdAndDeviceIdOrderByTimestampAsc(String userId, Long deviceId);
}
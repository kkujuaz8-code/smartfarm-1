package com.example.smartfarmserver.repository;

import com.example.smartfarmserver.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {
    // 기본 CRUD 메서드 자동 생성됨
}
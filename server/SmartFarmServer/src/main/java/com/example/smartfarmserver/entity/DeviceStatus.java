package com.example.smartfarmserver.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_status")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceStatus {

    @Id
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "pump_status", length = 10, nullable = false)
    private String pumpStatus;     // "ON" 또는 "OFF"

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
}
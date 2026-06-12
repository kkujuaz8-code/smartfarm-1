package com.example.smartfarmserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double temperature;
    private Double humidity;

    @Column(name = "soil_moisture")
    private Double soilMoisture;

    private LocalDateTime timestamp;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "device_id")
    private Long deviceId;
}
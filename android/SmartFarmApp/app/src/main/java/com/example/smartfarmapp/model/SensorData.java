package com.example.smartfarmapp.model;

import com.google.gson.annotations.SerializedName;

public class SensorData {
    private double temperature;
    private double humidity;

    // 🌟 [추가] JSON 이름(soilMoisture)과 매칭
    @SerializedName("soilMoisture")
    private double soilMoisture;

    private String timestamp;

    // Getter
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public double getSoilMoisture() { return soilMoisture; } // 🌟 추가
    public String getTimestamp() { return timestamp; }
}
package com.example.smartfarmserver.service;

import com.example.smartfarmserver.entity.ControlCommand; // 🌟 추가됨
import com.example.smartfarmserver.entity.DeviceStatus;
import com.example.smartfarmserver.repository.ControlCommandRepository; // 🌟 추가됨
import com.example.smartfarmserver.repository.DeviceStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceStatusRepository deviceStatusRepository;
    private final ControlCommandRepository controlCommandRepository; 
    
    private final Map<Long, Timer> autoOffTimers = new ConcurrentHashMap<>();
    private final long SERVER_MAX_RUN_TIME_MS = 15000; 

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void controlPump(Long deviceId, String state, String userId) {
        String command = state.toUpperCase();

        ControlCommand history = controlCommandRepository.save(ControlCommand.builder()
                .deviceId(deviceId)
                .targetState(command)
                .status("PENDING")
                .requestedBy(userId)
                .requestedAt(LocalDateTime.now())
                .build());

        try {

            sendCommandToArduino(deviceId, command);

            history.setStatus("SUCCESS");
            history.setExecutedAt(LocalDateTime.now());

            updatePumpStatus(deviceId, command);

            if ("ON".equals(command)) {
                startAutoOffTimer(deviceId, userId); 
                cancelAutoOffTimer(deviceId);
            }

        } catch (Exception e) {
            history.setStatus("FAILED");
            history.setFailReason(e.getMessage());
            log.error("❌ [IoT] 기기 #{} 제어 실패 사유: {}", deviceId, e.getMessage());
        }

     
        controlCommandRepository.save(history);
    }


    @Transactional
    public void updatePumpStatus(Long deviceId, String state) {
        DeviceStatus status = deviceStatusRepository.findById(deviceId)
                .orElseGet(() -> DeviceStatus.builder()
                        .deviceId(deviceId)
                        .pumpStatus("OFF")
                        .lastUpdatedAt(LocalDateTime.now())
                        .build());

        status.setPumpStatus(state.toUpperCase());
        status.setLastUpdatedAt(LocalDateTime.now());

        deviceStatusRepository.save(status);
        log.info("💾 [DB] 기기 #{} 상태 업데이트 완료: {}", deviceId, state);
    }


    private void startAutoOffTimer(Long deviceId, String originalUser) {
        cancelAutoOffTimer(deviceId); 
        
        Timer timer = new Timer();
        autoOffTimers.put(deviceId, timer);
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.error("🚨 [Safety] 기기 #{} 앱 응답 없음! 서버가 펌프를 강제 정지합니다.", deviceId);
                controlPump(deviceId, "OFF", "SYSTEM_SAFETY"); 
            }
        }, SERVER_MAX_RUN_TIME_MS);
    }

    private void cancelAutoOffTimer(Long deviceId) {
        Timer timer = autoOffTimers.remove(deviceId);
        if (timer != null) timer.cancel();
    }

    private void sendCommandToArduino(Long deviceId, String command) {
        String RASPBERRY_PI_IP = "100.64.229.77"; 
        String RASPBERRY_PI_PORT = "5000";       

        String targetUrl = String.format("http://%s:%s/hardware/pump?state=%s", 
                                          RASPBERRY_PI_IP, RASPBERRY_PI_PORT, command);

        log.info("➡️ [IoT] 명령 전송: {}", targetUrl);

        // restTemplate 호출 시 에러가 나면 catch문으로 던져지도록 처리됨
        restTemplate.getForObject(targetUrl, String.class);
        log.info("✅ [IoT] 하드웨어 명령 전달 성공");
    }
}
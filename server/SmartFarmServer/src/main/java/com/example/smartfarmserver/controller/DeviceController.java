package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    @PostMapping("/pump")
    public String requestPump(
            @RequestParam Long deviceId,
            @RequestParam String state,
            // 기본값을 "Guest"로 설정해서 혹시나 아이디가 안 넘어와도 에러 안 나게 방어!
            @RequestParam(required = false, defaultValue = "Guest") String userId) { 

        String command = state.toUpperCase();

        if (!command.equals("ON") && !command.equals("OFF")) {
            log.warn("🚨 잘못된 요청 - DeviceID: {}, Command: {}", deviceId, command);
            return "잘못된 명령어입니다. 'ON' 또는 'OFF'를 입력해주세요.";
        }
        deviceService.controlPump(deviceId, command, userId);
        log.info("✅ 처리 완료 - User: {}, Device: {}, Command: {}", userId, deviceId, command);
        return "펌프 상태 변경 및 로그 기록 완료: " + command;
    }
}
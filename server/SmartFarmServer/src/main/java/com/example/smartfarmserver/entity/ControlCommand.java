package com.example.smartfarmserver.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "control_command")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_id")
    private Long commandId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "target_state")
    private String targetState; // "ON" / "OFF"

    @Column(name = "status")
    private String status;      // "PENDING", "SUCCESS", "FAILED"

    @Column(name = "requested_by")
    private String requestedBy; // 🌟 닉네임/ID 저장 (문자열)

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "fail_reason")
    private String failReason;
}
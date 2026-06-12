package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.dto.LoginRequest;
import com.example.smartfarmserver.dto.LoginResponse;
import com.example.smartfarmserver.entity.Member;
import com.example.smartfarmserver.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 🌟 추가됨
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    // 🌟 우리가 만든 암호화 도구를 불러옵니다.
    @Autowired
    private PasswordEncoder passwordEncoder; 

    // 0. 아이디 중복 확인 (회원가입 전용)
    @GetMapping("/check-id")
    public LoginResponse checkId(@RequestParam String userId) {
        Member existing = memberRepository.findByMemberId(userId);
        if (existing != null) {
            return new LoginResponse(false, "이미 존재하는 아이디입니다.", null);
        }
        return new LoginResponse(true, "사용 가능한 아이디입니다.", userId);
    }

    // 1. 로그인 
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        System.out.println("🔥 로그인 요청: " + request.getUserId());

        // 안드로이드가 보낸 userId로 DB 검색
        Member member = memberRepository.findByMemberId(request.getUserId());

        // 🌟 [수정됨] 비밀번호 비교 방식 변경!
        // 평문(equals) 비교가 아니라, 암호화된 비밀번호가 일치하는지 matches()로 검사합니다.
        if (member != null && passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            // 성공
            return new LoginResponse(true, "로그인 성공", member.getMemberId());
        } else {
            // 실패
            return new LoginResponse(false, "아이디 또는 비밀번호가 일치하지 않습니다.", null);
        }
    }

    // 2. 회원가입 
    @PostMapping("/join")
    public LoginResponse join(@RequestBody LoginRequest request) {
        System.out.println("📝 회원가입 요청: " + request.getUserId());

        try {
            if (memberRepository.findByMemberId(request.getUserId()) != null) {
                return new LoginResponse(false, "이미 존재하는 아이디입니다.", null);
            }
            Member member = new Member();
            member.setMemberId(request.getUserId()); 
            
            // 🌟 [수정됨] 비밀번호를 암호화(encode)해서 저장합니다! (핵심)
            member.setPassword(passwordEncoder.encode(request.getPassword()));
            
            member.setName(request.getName());
            member.setEmail(request.getEmail());

            memberRepository.save(member);
            System.out.println("💾 TiDB 저장 완료 (비밀번호 암호화됨): " + member.getMemberId());

            return new LoginResponse(true, "회원가입 성공!", member.getMemberId());

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResponse(false, "가입 에러: " + e.getMessage(), null);
        }
    }
}
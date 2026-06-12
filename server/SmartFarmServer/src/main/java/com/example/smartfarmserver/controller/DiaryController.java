package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.entity.Diary;
import com.example.smartfarmserver.repository.DiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryRepository diaryRepository;

    // 1. 일기 저장 
    @PostMapping("/save")
    public String saveDiary(@RequestBody Diary diary) {
        System.out.println("📖 일기 저장 요청: " + diary.getUserId() + " (폴더ID: " + diary.getFolderId() + ")");
        try {
            diaryRepository.save(diary);
            return "저장 성공";
        } catch (Exception e) {
            return "저장 실패: " + e.getMessage();
        }
    }

    // 2. 일기 목록 불러오기 
    @GetMapping("/list")
    public List<Diary> getDiaryList(
            @RequestParam String userId,
            @RequestParam(required = false) Long folderId) { 

        System.out.println("🔍 일기 조회 요청 - ID: " + userId + ", 폴더: " + folderId);

        if (folderId != null) {
            return diaryRepository.findByUserIdAndFolderIdOrderByDateDesc(userId, folderId);
        } else {
            return diaryRepository.findAllByUserIdOrderByDateDesc(userId);
        }
    }

    // 3. 일기 삭제
    @DeleteMapping("/delete/{id}")
    public String deleteDiary(@PathVariable Long id) {
        System.out.println("🗑️ 일기 삭제 요청 ID: " + id);
        try {
            diaryRepository.deleteById(id);
            return "삭제 성공";
        } catch (Exception e) {
            return "삭제 실패";
        }
    }
}
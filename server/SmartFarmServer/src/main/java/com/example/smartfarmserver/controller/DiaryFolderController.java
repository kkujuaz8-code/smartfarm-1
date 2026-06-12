package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.entity.DiaryFolder;
import com.example.smartfarmserver.repository.DiaryFolderRepository;
import com.example.smartfarmserver.repository.DiaryRepository; // 🌟 추가 필요
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class DiaryFolderController {

    @Autowired
    private DiaryFolderRepository folderRepository;
    
    // 일기장 도구
    @Autowired
    private DiaryRepository diaryRepository;

    // 내 폴더 목록 조회
    @GetMapping
    public List<DiaryFolder> getFolders(@RequestParam String userId) {
        return folderRepository.findByUserId(userId);
    }

    // 폴더 생성
    @PostMapping
    public DiaryFolder createFolder(@RequestBody DiaryFolder folder) {
        return folderRepository.save(folder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFolder(@PathVariable Long id) {
        System.out.println("🗑️ 폴더 삭제 요청: " + id);

        diaryRepository.deleteByFolderId(id);
        
        folderRepository.deleteById(id);
        
        return ResponseEntity.ok("삭제 완료");
    }
}
package com.example.smartfarmserver.repository; // 👈 패키지명 수정됨

import com.example.smartfarmserver.entity.DiaryFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaryFolderRepository extends JpaRepository<DiaryFolder, Long> {
    List<DiaryFolder> findByUserId(String userId);
}
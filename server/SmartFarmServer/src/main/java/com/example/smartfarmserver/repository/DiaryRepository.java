package com.example.smartfarmserver.repository;

import com.example.smartfarmserver.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // 🌟 필수!
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findAllByUserIdOrderByDateDesc(String userId);
    List<Diary> findByUserIdAndFolderIdOrderByDateDesc(String userId, Long folderId);

    @Transactional
    void deleteByFolderId(Long folderId);
}
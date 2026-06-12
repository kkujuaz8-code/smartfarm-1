package com.example.smartfarmserver.repository;

import com.example.smartfarmserver.entity.CustomDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomDictionaryRepository extends JpaRepository<CustomDictionary, Long> {
    // 🌟 이름에 해당 글자가 포함된 모든 데이터를 찾는 메서드 (Contain 추가)
    List<CustomDictionary> findByNameContaining(String name);
}
package com.example.smartfarmserver.repository; 
import com.example.smartfarmserver.entity.PestDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PestDictionaryRepository extends JpaRepository<PestDictionary, Long> {
    
    // 앱에서 검색창에 검색어(keyword)를 쳤을 때 이름이 포함된 결과를 찾아주는 메서드
    List<PestDictionary> findByNameContaining(String keyword);

}
package com.example.smartfarmserver.repository;

import com.example.smartfarmserver.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Member findByMemberId(String memberId);
}
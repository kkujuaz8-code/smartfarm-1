package com.example.smartfarmserver.repository;

import com.example.smartfarmserver.entity.ControlCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlCommandRepository extends JpaRepository<ControlCommand, Long> {
}
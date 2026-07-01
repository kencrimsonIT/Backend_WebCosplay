package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
}
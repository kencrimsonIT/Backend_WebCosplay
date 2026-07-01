package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.InsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {

    List<InsurancePlan> findByIsActiveTrueOrderByFeeAmountAsc();
}
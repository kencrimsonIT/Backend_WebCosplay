package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserEmailOrderByCreatedAtDesc(String email);
    List<Complaint> findAllByOrderByCreatedAtDesc();
}

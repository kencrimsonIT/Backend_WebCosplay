package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.AdminComplaintResolveRequest;
import com.example.thuedocosplay.dto.request.ComplaintRequest;
import com.example.thuedocosplay.dto.response.ComplaintResponse;
import com.example.thuedocosplay.entity.Complaint;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.ComplaintStatus;
import com.example.thuedocosplay.entity.enums.ComplaintCategory;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ComplaintRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    @Transactional
    public ComplaintResponse createComplaint(String userEmail, ComplaintRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Complaint complaint = Complaint.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(ComplaintStatus.PENDING)
                .build();

        complaint = complaintRepository.save(complaint);
        return mapToResponse(complaint);
    }

    public List<ComplaintResponse> getMyComplaints(String userEmail) {
        return complaintRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResponse resolveComplaint(Long id, AdminComplaintResolveRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaint.setStatus(request.getStatus());
        if (request.getAdminResponse() != null) {
            complaint.setAdminResponse(request.getAdminResponse());
        }

        complaint = complaintRepository.save(complaint);
        return mapToResponse(complaint);
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUser().getId())
                .userEmail(complaint.getUser().getEmail())
                .title(complaint.getTitle())
                .category(complaint.getCategory())
                .description(complaint.getDescription())
                .adminResponse(complaint.getAdminResponse())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}

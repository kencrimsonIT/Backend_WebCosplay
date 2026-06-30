package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.AdminComplaintResolveRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.ComplaintResponse;
import com.example.thuedocosplay.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ApiResponse<List<ComplaintResponse>> getAllComplaints() {
        return ApiResponse.ok(complaintService.getAllComplaints());
    }

    @PutMapping("/{id}/resolve")
    public ApiResponse<ComplaintResponse> resolveComplaint(@PathVariable Long id, @Valid @RequestBody AdminComplaintResolveRequest request) {
        return ApiResponse.ok(complaintService.resolveComplaint(id, request));
    }
}

package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.ComplaintRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.ComplaintResponse;
import com.example.thuedocosplay.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ApiResponse<ComplaintResponse> create(@Valid @RequestBody ComplaintRequest request, Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ApiResponse.ok(complaintService.createComplaint(currentUserEmail, request));
    }

    @GetMapping("/my-complaints")
    public ApiResponse<List<ComplaintResponse>> getMyComplaints(Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ApiResponse.ok(complaintService.getMyComplaints(currentUserEmail));
    }
}

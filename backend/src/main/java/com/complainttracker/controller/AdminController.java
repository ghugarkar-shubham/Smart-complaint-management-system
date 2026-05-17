package com.complainttracker.controller;

import com.complainttracker.dto.ComplaintRequests.StatusUpdateRequest;
import com.complainttracker.dto.Responses.ComplaintResponse;
import com.complainttracker.dto.Responses.ComplaintStatusUpdateResponse;
import com.complainttracker.dto.Responses.SummaryResponse;
import com.complainttracker.service.AdminService;
import com.complainttracker.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final ComplaintService complaintService;
  private final AdminService adminService;

  public AdminController(ComplaintService complaintService, AdminService adminService) {
    this.complaintService = complaintService;
    this.adminService = adminService;
  }

  @GetMapping("/complaints")
  public ResponseEntity<List<ComplaintResponse>> all(@RequestParam(required = false) String search, @RequestParam(required = false) String status) {
    return ResponseEntity.ok(complaintService.getAdminComplaints(search, status));
  }

  @PutMapping("/complaints/{id}/status")
  public ResponseEntity<ComplaintStatusUpdateResponse> updateStatus(@PathVariable @NonNull Long id, @Valid @RequestBody StatusUpdateRequest request) {
    return ResponseEntity.ok(complaintService.updateStatus(id, request));
  }

  @DeleteMapping("/complaints/{id}")
  public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
    complaintService.deleteComplaint(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/summary")
  public ResponseEntity<SummaryResponse> summary() {
    return ResponseEntity.ok(adminService.summary());
  }
}

package com.complainttracker.service;

import com.complainttracker.dto.Responses.SummaryResponse;
import com.complainttracker.model.ComplaintStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
  private final ComplaintService complaintService;

  public AdminService(ComplaintService complaintService) {
    this.complaintService = complaintService;
  }

  public SummaryResponse summary() {
    long total = complaintService.countAll();
    long open = complaintService.countByStatus(ComplaintStatus.OPEN);
    long inProgress = complaintService.countByStatus(ComplaintStatus.IN_PROGRESS);
    long resolved = complaintService.countByStatus(ComplaintStatus.RESOLVED);
    return new SummaryResponse(total, open, inProgress, resolved);
  }
}

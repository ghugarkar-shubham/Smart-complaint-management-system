package com.complainttracker.dto;

import java.time.LocalDateTime;

public class Responses {
  public record AuthResponse(String token, String role, String fullName, String email) {}
  public record ApiResponse(String message) {}
  public record OtpResponse(String message, String email, boolean verified) {}
  public record ComplaintResponse(Long id, String title, String category, String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt, String submittedBy, String resolvedBy, String photoFilename) {}
  public record EmailNotificationResponse(boolean sent, String message, String providerResponse) {}
  public record ComplaintStatusUpdateResponse(ComplaintResponse complaint, EmailNotificationResponse emailNotification) {}
  public record SummaryResponse(long total, long open, long inProgress, long resolved) {}
}


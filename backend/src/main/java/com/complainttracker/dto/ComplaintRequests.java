package com.complainttracker.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintRequests {
  public record ComplaintRequest(@NotBlank String title, @NotBlank String category, @NotBlank String description) {}
  public record StatusUpdateRequest(@NotBlank String status) {}
}

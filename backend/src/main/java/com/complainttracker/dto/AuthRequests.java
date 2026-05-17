package com.complainttracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequests {
  public record RegisterRequest(@NotBlank(message = "Full name is required") String fullName, @Email @NotBlank String email, @Size(min = 6) @NotBlank String password) {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
}

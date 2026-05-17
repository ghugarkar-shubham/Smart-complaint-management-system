package com.complainttracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data transfer objects for authentication requests.
 * Email is the primary identifier for user registration and login.
 */
public class AuthRequests {
  public record RegisterRequest(
      @NotBlank(message = "Full name is required") String fullName,
      @Email @NotBlank String email,
      @Size(min = 6) @NotBlank String password) {}
  
  public record SendOtpRequest(
      @Email @NotBlank(message = "Email is required") String email) {}
  
  public record VerifyOtpRequest(
      @Email @NotBlank(message = "Email is required") String email,
      @NotBlank(message = "OTP is required") @Size(min = 6, max = 6, message = "OTP must be 6 digits") String otp) {}
  
  public record LoginRequest(
      @Email @NotBlank String email,
      @NotBlank String password) {}
}


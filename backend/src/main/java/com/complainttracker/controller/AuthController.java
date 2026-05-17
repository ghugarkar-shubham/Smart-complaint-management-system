package com.complainttracker.controller;

import com.complainttracker.dto.AuthRequests.LoginRequest;
import com.complainttracker.dto.AuthRequests.RegisterRequest;
import com.complainttracker.dto.AuthRequests.SendOtpRequest;
import com.complainttracker.dto.AuthRequests.VerifyOtpRequest;
import com.complainttracker.dto.Responses.ApiResponse;
import com.complainttracker.dto.Responses.AuthResponse;
import com.complainttracker.dto.Responses.OtpResponse;
import com.complainttracker.service.AuthService;
import com.complainttracker.service.EmailOtpVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for authentication endpoints.
 * Handles user registration with email OTP verification and login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final EmailOtpVerificationService emailOtpVerificationService;

  public AuthController(AuthService authService, EmailOtpVerificationService emailOtpVerificationService) {
    this.authService = authService;
    this.emailOtpVerificationService = emailOtpVerificationService;
  }

  /**
   * Send OTP to user's email for verification.
   * Called as first step of registration.
   *
   * @param request SendOtpRequest with email
   * @return OtpResponse with status message
   */
  @PostMapping("/send-otp")
  public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
    return ResponseEntity.ok(emailOtpVerificationService.sendOtp(request.email()));
  }

  /**
   * Verify OTP sent to user's email.
   * Called after user receives OTP in their email.
   *
   * @param request VerifyOtpRequest with email and OTP
   * @return OtpResponse with verification status
   */
  @PostMapping("/verify-otp")
  public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    return ResponseEntity.ok(emailOtpVerificationService.verifyOtp(request.email(), request.otp()));
  }

  /**
   * Resend OTP to user's email.
   * User can request new OTP if previous one expired.
   *
   * @param request SendOtpRequest with email
   * @return OtpResponse with status message
   */
  @PostMapping("/resend-otp")
  public ResponseEntity<OtpResponse> resendOtp(@Valid @RequestBody SendOtpRequest request) {
    return ResponseEntity.ok(emailOtpVerificationService.resendOtp(request.email()));
  }

  /**
   * Register new user after email OTP verification.
   * Email must be verified via OTP before this endpoint can succeed.
   *
   * @param request RegisterRequest with fullName, email, and password
   * @return ApiResponse with status message
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.ok(new ApiResponse("Registration successful"));
  }

  /**
   * User login with email and password.
   * Returns JWT token for authenticated access.
   *
   * @param request LoginRequest with email and password
   * @return AuthResponse with JWT token and user details
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}

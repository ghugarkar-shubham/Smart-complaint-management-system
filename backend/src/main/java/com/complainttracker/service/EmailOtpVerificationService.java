package com.complainttracker.service;

import com.complainttracker.dto.Responses.OtpResponse;
import com.complainttracker.exception.BadRequestException;
import com.complainttracker.exception.ResourceNotFoundException;
import com.complainttracker.model.EmailOtpVerification;
import com.complainttracker.repository.EmailOtpVerificationRepository;
import com.complainttracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service to handle email-based OTP verification during user registration.
 * Manages OTP generation, validation, and consumption for email verification.
 */
@Service
public class EmailOtpVerificationService {
  private static final Logger log = LoggerFactory.getLogger(EmailOtpVerificationService.class);
  private static final SecureRandom RANDOM = new SecureRandom();

  private final EmailOtpVerificationRepository emailOtpRepository;
  private final UserRepository userRepository;
  private final EmailNotificationService emailNotificationService;
  private final PasswordEncoder passwordEncoder;
  private final int expirationMinutes;
  private final int maxAttempts;

  public EmailOtpVerificationService(EmailOtpVerificationRepository emailOtpRepository,
                                     UserRepository userRepository,
                                     EmailNotificationService emailNotificationService,
                                     PasswordEncoder passwordEncoder,
                                     @Value("${app.otp.expiration-minutes:10}") int expirationMinutes,
                                     @Value("${app.otp.max-attempts:5}") int maxAttempts) {
    this.emailOtpRepository = emailOtpRepository;
    this.userRepository = userRepository;
    this.emailNotificationService = emailNotificationService;
    this.passwordEncoder = passwordEncoder;
    this.expirationMinutes = expirationMinutes;
    this.maxAttempts = maxAttempts;
  }

  /**
   * Send OTP to user's email address.
   * Checks if email is already registered and prevents duplicate registrations.
   *
   * @param email The email to send OTP to
   * @return OtpResponse with status message
   * @throws BadRequestException if email is already registered
   */
  public OtpResponse sendOtp(String email) {
    email = email.toLowerCase().trim();
    
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("This email is already registered");
    }

    // Generate 6-digit OTP
    String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
    LocalDateTime expiresAt = LocalDateTime.now().plus(expirationMinutes, ChronoUnit.MINUTES);

    // Create or update OTP verification record
    EmailOtpVerification verification = emailOtpRepository.findByEmail(email)
        .orElseGet(EmailOtpVerification::new);
    verification.setEmail(email);
    verification.setOtpHash(passwordEncoder.encode(otp));
    verification.setExpiresAt(expiresAt);
    verification.setVerifiedAt(null);
    verification.setConsumedAt(null);
    verification.setAttempts(0);
    emailOtpRepository.save(verification);

    // Send OTP email
    emailNotificationService.sendOtpEmail(email, otp, expirationMinutes);
    log.info("OTP sent to {}", email);
    return new OtpResponse("OTP sent successfully to your email", email, false);
  }

  /**
   * Verify the OTP provided by user.
   * Validates OTP expiry, attempt limits, and correctness.
   *
   * @param email The email to verify
   * @param otp The OTP code entered by user
   * @return OtpResponse with verification status
   * @throws BadRequestException if OTP is invalid, expired, or attempts exceeded
   * @throws ResourceNotFoundException if OTP record not found
   */
  public OtpResponse verifyOtp(String email, String otp) {
    email = email.toLowerCase().trim();
    
    EmailOtpVerification verification = emailOtpRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("OTP request not found for this email"));

    // Check if already consumed
    if (verification.getConsumedAt() != null) {
      throw new BadRequestException("This email has already been verified and used for registration");
    }

    // Check if expired
    if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BadRequestException("OTP has expired. Please request a new one");
    }

    // Check attempt limit
    if (verification.getAttempts() >= maxAttempts) {
      throw new BadRequestException("Too many failed OTP attempts. Please request a new code");
    }

    // Verify OTP
    verification.setAttempts(verification.getAttempts() + 1);
    if (!passwordEncoder.matches(otp, verification.getOtpHash())) {
      emailOtpRepository.save(verification);
      throw new BadRequestException("Invalid OTP");
    }

    // Mark as verified
    verification.setVerifiedAt(LocalDateTime.now());
    emailOtpRepository.save(verification);
    log.info("OTP verified for {}", email);
    return new OtpResponse("OTP verified successfully", email, true);
  }

  /**
   * Check if an email has been verified via OTP.
   * Used during registration to ensure email verification before account creation.
   *
   * @param email The email to check
   * @throws BadRequestException if email is not verified or verification has expired
   */
  public void requireVerifiedEmail(String email) {
    email = email.toLowerCase().trim();
    
    EmailOtpVerification verification = emailOtpRepository.findByEmail(email)
        .orElseThrow(() -> new BadRequestException("Email is not verified"));

    if (verification.getVerifiedAt() == null || verification.getConsumedAt() != null) {
      throw new BadRequestException("Email is not verified");
    }
    if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BadRequestException("OTP verification has expired. Please verify again");
    }
  }

  /**
   * Mark verified email as consumed.
   * Called after successful user registration to prevent reuse of the same OTP.
   *
   * @param email The email to consume
   * @throws BadRequestException if email is not verified
   */
  public void consumeVerifiedEmail(String email) {
    email = email.toLowerCase().trim();
    
    EmailOtpVerification verification = emailOtpRepository.findByEmail(email)
        .orElseThrow(() -> new BadRequestException("Email is not verified"));

    if (verification.getVerifiedAt() == null || verification.getConsumedAt() != null) {
      throw new BadRequestException("Email is not verified");
    }
    verification.setConsumedAt(LocalDateTime.now());
    emailOtpRepository.save(verification);
    log.info("OTP consumed for {}", email);
  }

  /**
   * Resend OTP to email.
   * User can request new OTP if previous one expired.
   *
   * @param email The email to send new OTP to
   * @return OtpResponse with status message
   */
  public OtpResponse resendOtp(String email) {
    email = email.toLowerCase().trim();
    
    // Reset attempts and resend
    return sendOtp(email);
  }
}

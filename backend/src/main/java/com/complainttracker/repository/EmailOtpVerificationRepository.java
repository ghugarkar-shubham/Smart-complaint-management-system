package com.complainttracker.repository;

import com.complainttracker.model.EmailOtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for managing EmailOtpVerification entities.
 * Provides database access methods for email OTP verification records.
 */
public interface EmailOtpVerificationRepository extends JpaRepository<EmailOtpVerification, Long> {
  Optional<EmailOtpVerification> findByEmail(String email);
}

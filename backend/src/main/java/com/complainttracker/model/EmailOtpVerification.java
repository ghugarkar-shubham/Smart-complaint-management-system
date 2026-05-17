package com.complainttracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity to store email OTP verification records for user registration.
 * Tracks OTP generation, verification, and consumption for email-based verification flow.
 */
@Entity
@Table(name = "email_otp_verifications")
public class EmailOtpVerification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, length = 255)
  private String otpHash;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  private LocalDateTime verifiedAt;

  private LocalDateTime consumedAt;

  @Column(nullable = false)
  private int attempts;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getOtpHash() {
    return otpHash;
  }

  public void setOtpHash(String otpHash) {
    this.otpHash = otpHash;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public LocalDateTime getVerifiedAt() {
    return verifiedAt;
  }

  public void setVerifiedAt(LocalDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
  }

  public LocalDateTime getConsumedAt() {
    return consumedAt;
  }

  public void setConsumedAt(LocalDateTime consumedAt) {
    this.consumedAt = consumedAt;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}

-- ============================================
-- Database Migration: SMS to Email Integration
-- ============================================
-- This migration converts the system from SMS-based verification to email-based OTP.
-- It adds email verification support and creates new tables for email OTP tracking.

-- Create new table for email OTP verifications
CREATE TABLE IF NOT EXISTS email_otp_verifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(160) NOT NULL UNIQUE,
  otp_hash VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  verified_at DATETIME,
  consumed_at DATETIME,
  attempts INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add email_verified column to users table if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT false;

-- Update existing users to mark them as email verified (for backward compatibility)
UPDATE users SET email_verified = true WHERE id > 0;

-- ============================================
-- Notes:
-- 1. The mobile_otp_verifications table is kept for historical data but no longer used
-- 2. Users can keep their mobile_number field for reference but it's optional now
-- 3. Email is now the primary identifier for authentication
-- 4. Old OTP records can be archived or deleted after migration period
-- ============================================

-- Optional: Archive old SMS data (commented out by default)
-- ALTER TABLE mobile_otp_verifications RENAME TO mobile_otp_verifications_archive;

-- Optional: Create triggers for automatic table cleanup
-- DELETE FROM email_otp_verifications WHERE consumed_at IS NOT NULL AND consumed_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

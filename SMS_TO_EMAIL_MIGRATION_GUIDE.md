# SMS to Email Migration Implementation Guide

## Overview
This document outlines the complete migration from SMS-based verification (Fast2SMS) to email-based verification using Gmail SMTP and JavaMailSender.

---

## 1. What Was Changed

### Backend Changes

#### 1.1 Dependencies (pom.xml)
- ✅ Removed: Fast2SMS client dependencies (if any)
- ✅ Added: `spring-boot-starter-mail` - For email functionality

#### 1.2 Configuration (application.properties)
- ✅ Removed: `fast2sms.api-url` and `fast2sms.api-key`
- ✅ Added: Gmail SMTP configuration
  - `spring.mail.host` - SMTP server
  - `spring.mail.port` - Port number
  - `spring.mail.username` - Gmail address
  - `spring.mail.password` - Gmail app password
  - Email sending properties

#### 1.3 Models
- ✅ Created: `EmailOtpVerification.java` - Replaces `MobileOtpVerification`
  - Tracks OTP for email-based verification
  - Fields: email, otpHash, expiresAt, verifiedAt, consumedAt, attempts
  
- ✅ Updated: `User.java`
  - Added: `emailVerified` field to track email verification status
  - Kept: `mobileNumber` field (optional, for backward compatibility)

#### 1.4 Services
- ✅ Created: `EmailNotificationService.java`
  - Sends OTP verification emails during registration
  - Sends status update notifications when complaint status changes
  - Uses JavaMailSender for SMTP delivery
  - Includes professional email templates
  
- ✅ Created: `EmailOtpVerificationService.java` (replaces old OtpVerificationService)
  - Generates and validates email OTPs
  - Handles OTP expiration and attempt limits
  - Integrates with EmailNotificationService for delivery
  - Methods:
    - `sendOtp(email)` - Send OTP to email
    - `verifyOtp(email, otp)` - Verify user-entered OTP
    - `requireVerifiedEmail(email)` - Check if email is verified
    - `consumeVerifiedEmail(email)` - Mark OTP as used
    - `resendOtp(email)` - Resend OTP if expired

- ✅ Updated: `AuthService.java`
  - Uses `EmailOtpVerificationService` instead of OTP verification
  - Registration now requires email OTP verification
  - Sets `emailVerified = true` after successful registration

- ✅ Updated: `AuthController.java`
  - Updated endpoints to use email instead of mobile
  - New endpoint: `/api/auth/resend-otp` - For resending OTP
  - All OTP-related endpoints now use email

- ✅ Updated: `ComplaintService.java`
  - Uses `EmailNotificationService` for status updates
  - Sends email when complaint status changes
  - Replaces SMS notifications with email notifications

#### 1.5 DTOs
- ✅ Updated: `AuthRequests.java`
  - `RegisterRequest`: Removed `mobileNumber`, kept `email`
  - `SendOtpRequest`: Changed from mobile to email
  - `VerifyOtpRequest`: Changed from mobile to email
  - `LoginRequest`: Already email-based (no change)

- ✅ Updated: `Responses.java`
  - `OtpResponse`: Changed `mobileNumber` to `email`
  - Renamed: `SmsNotificationResponse` → `EmailNotificationResponse`
  - Updated: `ComplaintStatusUpdateResponse` to use `EmailNotificationResponse`

#### 1.6 Repositories
- ✅ Created: `EmailOtpVerificationRepository.java`
  - New repository for email OTP management
  - Method: `findByEmail(String email)`

### Frontend Changes

#### 2.1 Authentication Service (authService.js)
- ✅ Updated: `sendOtp()` - Now sends email instead of mobile
- ✅ Updated: `verifyOtp()` - Verifies email OTP
- ✅ Added: `resendOtp()` - Resend OTP if expired
- ✅ Added: Comments documenting each function

#### 2.2 Registration Page (RegisterPage.jsx)
- ✅ Completely redesigned with email OTP flow:
  - Step 1: User enters email
  - Step 2: User clicks "Send OTP" → OTP sent to email
  - Step 3: User enters OTP from email
  - Step 4: After verification, user enters name and password
  - Step 5: User submits registration
  
- ✅ Features:
  - Email validation
  - OTP input field (only shown after OTP is sent)
  - "Resend OTP" link if user didn't receive email
  - Conditional rendering of form fields based on verification status
  - Loading states and error/success messages
  - Responsive design maintained

### Database Changes

#### 3.1 New Tables
- ✅ Created: `email_otp_verifications` table
  - Replaces `mobile_otp_verifications` for email-based verification
  - Stores email, OTP hash, expiration, verification timestamps, attempt count

#### 3.2 Updated Tables
- ✅ Users: Added `email_verified` column

---

## 2. Gmail SMTP Setup

### Prerequisites
- Gmail account (personal or Google Workspace)
- Gmail must have 2-factor authentication enabled

### Step-by-Step Setup

#### 2.1 Enable 2-Factor Authentication
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable 2-Step Verification if not already enabled

#### 2.2 Generate App Password
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Scroll to "App passwords" (only visible if 2FA is enabled)
3. Select: Device = "Windows Computer" (or your OS), App = "Mail"
4. Google will generate a 16-character password
5. Copy this password (you'll use it as `MAIL_PASSWORD`)

#### 2.3 Configure application.properties
Option 1: Set directly in application.properties (development only)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

Option 2: Use environment variables (recommended for production)
```bash
# Linux/Mac
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export MAIL_FROM=your-email@gmail.com

# Windows PowerShell
$env:MAIL_HOST = "smtp.gmail.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "your-email@gmail.com"
$env:MAIL_PASSWORD = "your-app-password"
$env:MAIL_FROM = "your-email@gmail.com"
```

#### 2.4 Verify Configuration
The application.properties already includes:
```properties
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

---

## 3. Backend API Endpoints

### Authentication Endpoints

#### Send OTP
```
POST /api/auth/send-otp
Content-Type: application/json

{
  "email": "user@example.com"
}

Response: 200 OK
{
  "message": "OTP sent successfully to your email",
  "email": "user@example.com",
  "verified": false
}
```

#### Verify OTP
```
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}

Response: 200 OK
{
  "message": "OTP verified successfully",
  "email": "user@example.com",
  "verified": true
}
```

#### Resend OTP
```
POST /api/auth/resend-otp
Content-Type: application/json

{
  "email": "user@example.com"
}

Response: 200 OK
{
  "message": "OTP sent successfully to your email",
  "email": "user@example.com",
  "verified": false
}
```

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "message": "Registration successful"
}

Note: Email must be verified via OTP before registration succeeds
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "fullName": "John Doe",
  "email": "user@example.com"
}
```

---

## 4. Email Notifications

### OTP Verification Email
Sent when user requests OTP during registration.

**Subject:** Email Verification OTP - Complaint Tracker

**Body:**
```
Dear User,

Thank you for registering with Complaint Tracker.

Your OTP for email verification is: 123456
This OTP will expire in 10 minutes.

If you did not request this OTP, please ignore this email.

Best regards,
Complaint Tracker
Support Team
```

### Complaint Status Update Email
Sent when admin updates complaint status (OPEN, IN_PROGRESS, RESOLVED).

**Subject:** Complaint Status Update - ID #1

**Body:**
```
Dear John Doe,

We are writing to notify you that your complaint has been updated.

Complaint Details:
- Complaint ID: #1
- Title: Issue with complaint system
- Current Status: RESOLVED
- Updated On: 2024-05-17T10:30:00

Your complaint is being actively monitored and will be resolved at the earliest.

Best regards,
Complaint Tracker
Support Team
```

---

## 5. OTP Configuration

The following settings control OTP behavior:

```properties
# OTP expiration time in minutes
app.otp.expiration-minutes=10

# Maximum failed verification attempts before blocking
app.otp.max-attempts=5
```

**Behavior:**
- OTP valid for 10 minutes from generation
- User gets 5 attempts to enter correct OTP
- After 5 failed attempts, OTP is invalidated
- User must request new OTP

---

## 6. Database Migration

### Apply Migration
Execute the SQL file to add new tables and columns:

```sql
-- From command line:
mysql -u root -p complaint_system < database/migration_sms_to_email.sql

-- Or from MySQL client:
source database/migration_sms_to_email.sql;
```

### Migration Actions
1. Creates `email_otp_verifications` table
2. Adds `email_verified` column to `users` table
3. Marks existing users as email_verified (backward compatibility)

---

## 7. Testing the Implementation

### Frontend Testing

#### Registration Flow
1. Navigate to `/register`
2. Enter email address
3. Click "Send OTP"
4. Check your email for OTP (check spam folder if not found)
5. Enter OTP in the form
6. Click "Verify"
7. After verification, enter full name and password
8. Click "Register"
9. Should redirect to login page

#### Resend OTP
1. If you don't receive OTP, click "Resend OTP" link
2. OTP will be sent again to the same email

#### Login Flow
1. Navigate to `/login`
2. Enter verified email and password
3. Click "Login"
4. Should redirect to dashboard

### Backend Testing

#### Using cURL or Postman

```bash
# Step 1: Send OTP
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# Step 2: Verify OTP (get OTP from email first)
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","otp":"123456"}'

# Step 3: Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"password123"}'

# Step 4: Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 8. Error Handling

### Common Errors and Solutions

#### Error: "OTP request not found for this email"
- **Cause:** OTP not sent for this email first
- **Solution:** Click "Send OTP" first, then enter the OTP you receive

#### Error: "OTP has expired"
- **Cause:** More than 10 minutes have passed since OTP was sent
- **Solution:** Click "Resend OTP" to get a new OTP

#### Error: "Too many failed OTP attempts"
- **Cause:** Entered wrong OTP 5 times
- **Solution:** Click "Resend OTP" to get a new OTP and try again

#### Error: "Email already registered"
- **Cause:** Email is already used for another account
- **Solution:** Use a different email or login if you already have an account

#### Error: "This email is already registered"
- **Cause:** Trying to send OTP for an email that's already registered
- **Solution:** Use a different email for registration

#### Email Not Arriving
- **Check:** Spam/Junk folder
- **Check:** Email address is correct
- **Check:** Gmail credentials and app password are correct in application.properties
- **Check:** 2-Factor Authentication is enabled on Gmail account
- **Check:** App password is used (not regular Gmail password)

---

## 9. Security Considerations

### Best Practices Implemented

1. **OTP Hashing:** OTPs are hashed using BCrypt, not stored in plain text
2. **Expiration:** OTPs expire after 10 minutes
3. **Attempt Limits:** Maximum 5 failed attempts per OTP
4. **Email Verification:** Email must be verified before account creation
5. **One-Time Use:** OTP can only be consumed once for registration
6. **Password Encryption:** User passwords are hashed with BCrypt
7. **JWT Tokens:** Sessions managed with JWT (no server-side session storage)

### Recommendations

1. **Environment Variables:** Store email credentials in environment variables, not in code
2. **HTTPS:** Use HTTPS in production to protect email/password transmission
3. **Rate Limiting:** Consider adding rate limiting to OTP endpoints to prevent abuse
4. **Email Verification Expiry:** Consider adding email re-verification annually
5. **Audit Logging:** Log all authentication events for security monitoring

---

## 10. Troubleshooting

### Issue: Emails Not Sending

#### Check 1: Gmail Configuration
```bash
# Verify Gmail credentials are correct
echo "Username: your-email@gmail.com"
echo "Password: (copy from Google Account > App passwords)"
```

#### Check 2: Application Properties
Verify `application.properties` has:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Check 3: Logs
Look for error messages in Spring Boot logs:
```
ERROR ... MailSendException: Failed to send email
```

#### Check 4: Gmail Settings
- Ensure 2-Factor Authentication is enabled
- Ensure App Password was generated (not regular password)
- Check if Less Secure Apps is disabled (newer Gmail accounts)

### Issue: OTP Not Verifying

#### Check 1: Copy Exact OTP
- Make sure you're copying the exact 6-digit OTP from email
- Exclude any extra spaces or characters

#### Check 2: Check OTP Expiry
- OTP expires after 10 minutes
- If expired, click "Resend OTP"

#### Check 3: Attempt Limit
- If you've entered wrong OTP 5 times, you need to resend
- Click "Resend OTP" button

---

## 11. Production Deployment Checklist

- [ ] Configure environment variables for email credentials (not in code)
- [ ] Ensure MySQL database has migration applied
- [ ] Run application and verify Spring Boot starts without errors
- [ ] Test registration flow end-to-end
- [ ] Test login with registered account
- [ ] Test complaint status updates send emails
- [ ] Monitor email delivery (check logs for send failures)
- [ ] Set up logging/monitoring for authentication events
- [ ] Configure rate limiting on auth endpoints
- [ ] Set up automated database backups
- [ ] Enable HTTPS on production server
- [ ] Configure email account rotation for security

---

## 12. Summary of Files Changed

### Backend
- `pom.xml` - Added mail dependency
- `src/main/resources/application.properties` - Updated email config
- `src/main/java/com/complainttracker/model/EmailOtpVerification.java` - NEW
- `src/main/java/com/complainttracker/model/User.java` - Updated
- `src/main/java/com/complainttracker/service/EmailNotificationService.java` - NEW
- `src/main/java/com/complainttracker/service/EmailOtpVerificationService.java` - NEW
- `src/main/java/com/complainttracker/service/AuthService.java` - Updated
- `src/main/java/com/complainttracker/service/ComplaintService.java` - Updated
- `src/main/java/com/complainttracker/controller/AuthController.java` - Updated
- `src/main/java/com/complainttracker/dto/AuthRequests.java` - Updated
- `src/main/java/com/complainttracker/dto/Responses.java` - Updated
- `src/main/java/com/complainttracker/repository/EmailOtpVerificationRepository.java` - NEW

### Frontend
- `src/services/authService.js` - Updated with email OTP methods
- `src/pages/RegisterPage.jsx` - Completely redesigned for email OTP flow

### Database
- `database/migration_sms_to_email.sql` - NEW migration file

---

## 13. Support & Feedback

For issues or questions:
1. Check the Troubleshooting section above
2. Review email configuration in Gmail account settings
3. Check Spring Boot logs for detailed error messages
4. Verify database migration was applied successfully

---

**Last Updated:** May 17, 2024  
**Version:** 1.0

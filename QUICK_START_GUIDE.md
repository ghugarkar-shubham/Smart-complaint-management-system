# SMS to Email Implementation - Quick Start Guide

## ✅ What's Been Completed

All SMS functionality has been removed and replaced with email-based verification and notifications. Here's what you need to do to get it running.

---

## 🚀 Step 1: Configure Gmail SMTP (5 minutes)

### 1.1 Enable 2-Factor Authentication on Gmail
1. Go to https://myaccount.google.com/security
2. Enable "2-Step Verification" (if not already enabled)

### 1.2 Generate App Password
1. In Google Account Security, find "App passwords"
2. Select Device: "Windows Computer" (or your OS)
3. Select App: "Mail"
4. Copy the 16-character password generated

### 1.3 Update application.properties
Edit `backend/src/main/resources/application.properties`:

```properties
# Replace these values with your Gmail credentials:
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
app.mail.from=your-email@gmail.com
```

**Alternative (using environment variables - Recommended for Production):**
```bash
# Windows PowerShell:
$env:MAIL_USERNAME = "your-email@gmail.com"
$env:MAIL_PASSWORD = "your-16-char-app-password"
$env:MAIL_FROM = "your-email@gmail.com"
```

---

## 🗄️ Step 2: Apply Database Migration (2 minutes)

### 2.1 Run Migration SQL
```bash
# From your database directory:
mysql -u root -p complaint_system < database/migration_sms_to_email.sql
```

Or use MySQL Workbench/Client to execute:
```sql
-- Copy contents of database/migration_sms_to_email.sql and execute
```

**What it does:**
- Creates `email_otp_verifications` table for email OTP tracking
- Adds `email_verified` column to `users` table
- Maintains backward compatibility

---

## 🔨 Step 3: Build Backend (3 minutes)

### 3.1 Install Dependencies
```bash
cd backend
./mvnw clean install
```

### 3.2 Verify Build
```bash
./mvnw compile
```

---

## 🚀 Step 4: Start the Application

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

Expected output:
```
Tomcat started on port(s): 8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Expected output:
```
VITE v... ready in ... ms
➜  Local:   http://localhost:5173/
```

---

## 🧪 Step 5: Test Email Registration Flow

### 5.1 Test Registration
1. Open http://localhost:5173
2. Click "Create account" or go to `/register`
3. Enter your email address
4. Click "Send OTP"
5. **Check your email for OTP** (check spam folder if needed)
6. Enter the 6-digit OTP in the form
7. Click "Verify"
8. Enter full name and password
9. Click "Register"
10. Should redirect to login page

### 5.2 Test Login
1. On login page, enter your verified email and password
2. Click "Login"
3. Should redirect to dashboard (USER) or admin panel (ADMIN)

### 5.3 Test Email Notifications
1. Login as admin (if you have admin account)
2. Go to Admin Dashboard
3. Find a complaint and update its status
4. **Check your email** - You should receive notification about status update

---

## 📧 What Emails You'll Receive

### OTP Verification Email
When user requests OTP during registration.
```
Subject: Email Verification OTP - Complaint Tracker

Body:
Your OTP for email verification is: 123456
This OTP will expire in 10 minutes.
```

### Complaint Status Update Email
When admin updates complaint status (OPEN, IN_PROGRESS, RESOLVED).
```
Subject: Complaint Status Update - ID #1

Body:
Your complaint ID #1 status has been updated to RESOLVED
[Full complaint details included]
```

---

## 🔄 API Endpoints (For Integration)

### Send OTP
```
POST http://localhost:8080/api/auth/send-otp
{
  "email": "user@example.com"
}
```

### Verify OTP
```
POST http://localhost:8080/api/auth/verify-otp
{
  "email": "user@example.com",
  "otp": "123456"
}
```

### Register (after email verification)
```
POST http://localhost:8080/api/auth/register
{
  "fullName": "John Doe",
  "email": "user@example.com",
  "password": "securePassword123"
}
```

### Login
```
POST http://localhost:8080/api/auth/login
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

---

## ⚠️ If Emails Aren't Arriving

### Checklist
1. ✅ Check **Spam/Junk folder** in email account
2. ✅ Verify Gmail **2-Factor Authentication is enabled**
3. ✅ Verify **App Password was used** (not regular Gmail password)
4. ✅ Confirm **application.properties has correct credentials**
5. ✅ Check Spring Boot **logs for SMTP errors**

### Debug Command
```bash
# Check if SMTP connection works
telnet smtp.gmail.com 587
# Should show: 220 smtp.gmail.com ESMTP ready
```

### Log Error Example
```
ERROR ... MailSendException: Failed to send email
Caused by: javax.mail.AuthenticationFailedException: 535 5.7.8 Username and password not accepted
```

**Solution:** Wrong app password or not 2FA enabled.

---

## 📋 Removed SMS Components

The following SMS-related code has been removed:

### Removed Files
- `Fast2SmsClient.java` - SMS API client
- `SmsNotificationService.java` - SMS notifications
- `OtpVerificationService.java` - Old SMS OTP service (replaced with email version)
- `MobileOtpVerification.java` model - Old SMS OTP entity

### Removed Properties
```properties
# These are NO LONGER USED:
fast2sms.api-url=...
fast2sms.api-key=...
```

### Removed DTOs
- Old `SendOtpRequest` with mobileNumber
- Old `VerifyOtpRequest` with mobileNumber
- `SmsNotificationResponse` (replaced with EmailNotificationResponse)

---

## 📱 What You Can Delete (Optional)

After migration, you can optionally delete:
1. `mobile_otp_verifications` table (old SMS OTP records)
2. Mobile number from user registration forms (if not needed)
3. Any Fast2SMS-related code comments

---

## 🔐 Security Notes

### Email Credentials
- ✅ Use environment variables in production
- ✅ Never commit credentials to Git
- ✅ Use Gmail App Password (not regular password)
- ✅ Require 2-Factor Authentication on Gmail

### OTP Security
- ✅ OTPs are hashed with BCrypt
- ✅ OTPs expire after 10 minutes
- ✅ Maximum 5 failed attempts per OTP
- ✅ One-time use only

### Additional Recommendations
- ✅ Use HTTPS in production
- ✅ Consider rate limiting on auth endpoints
- ✅ Monitor authentication logs
- ✅ Regular security audits

---

## 📚 Documentation Files

For more detailed information, see:

1. **[SMS_TO_EMAIL_MIGRATION_GUIDE.md](SMS_TO_EMAIL_MIGRATION_GUIDE.md)** - Comprehensive implementation guide
   - Complete list of all changes
   - Detailed Gmail setup instructions
   - API endpoint documentation
   - Troubleshooting guide
   - Production deployment checklist

2. **[database/migration_sms_to_email.sql](database/migration_sms_to_email.sql)** - Database migration script

3. **Code Comments** - Each service and controller has detailed JSDoc comments explaining functionality

---

## ✨ New Features

### Email OTP Registration
- Users verify email before account creation
- Automated OTP delivery to email
- Resend OTP option if expired
- Clean, responsive registration UI

### Complaint Status Email Notifications
- Automatic email when status is updated
- Professional email template
- Includes complaint details
- Real-time notification delivery

### Enhanced Security
- Email-based verification instead of SMS
- Better OTP handling with hashing
- Attempt limits and expiration
- Email verification flag in user model

---

## 🎯 Next Steps

1. **Configure Gmail** - Follow Step 1 above
2. **Apply Database Migration** - Follow Step 2
3. **Build Backend** - Follow Step 3
4. **Start Application** - Follow Step 4
5. **Test Registration** - Follow Step 5
6. **Deploy to Production** - See production checklist in full guide

---

## 🆘 Need Help?

### Email Not Arriving?
1. Check spam folder
2. Verify Gmail 2FA is enabled
3. Verify app password (not regular password)
4. Check application.properties credentials
5. Review Spring Boot logs for errors

### OTP Not Verifying?
1. Copy exact 6-digit code from email
2. Make sure it's within 10-minute expiration
3. Check you haven't exceeded 5 attempts
4. Click "Resend OTP" if expired

### Registration Not Working?
1. Verify email is not already registered
2. Confirm OTP verification succeeded
3. Check password is at least 6 characters
4. Check Spring Boot logs for errors

### Look at Spring Boot Logs
```bash
# When running backend:
./mvnw spring-boot:run

# Watch for any errors containing "Mail" or "SMTP"
# Errors will show why email isn't sending
```

---

## 📞 Quick Reference

| Component | Old | New |
|-----------|-----|-----|
| Dependency | Fast2SMS API | JavaMailSender |
| Verification Method | Mobile OTP | Email OTP |
| Primary Identifier | Mobile Number | Email Address |
| OTP Table | mobile_otp_verifications | email_otp_verifications |
| Notification Method | SMS | Email |
| Config | fast2sms.* | spring.mail.* |

---

**Implementation Complete!** 🎉

Your Online Complaint Tracking System now uses professional email-based verification and notifications instead of SMS. Follow the steps above to get it running.

For detailed troubleshooting and production deployment, see the complete [SMS_TO_EMAIL_MIGRATION_GUIDE.md](SMS_TO_EMAIL_MIGRATION_GUIDE.md).

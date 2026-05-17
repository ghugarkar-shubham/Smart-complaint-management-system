package com.complainttracker.service;

import com.complainttracker.dto.Responses.EmailNotificationResponse;
import com.complainttracker.model.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service to send email notifications for complaint status updates.
 * Uses JavaMailSender with Gmail SMTP configuration.
 */
@Service
public class EmailNotificationService {
  private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

  private final JavaMailSender mailSender;
  private final String mailFrom;
  private final String mailFromName;

  public EmailNotificationService(JavaMailSender mailSender,
                                 @Value("${app.mail.from:noreply@complainttracker.com}") String mailFrom,
                                 @Value("${app.mail.from-name:Complaint Tracker}") String mailFromName) {
    this.mailSender = mailSender;
    this.mailFrom = mailFrom;
    this.mailFromName = mailFromName;
  }

  /**
   * Send email notification when complaint status is updated.
   * Email contains complaint ID, updated status, and professional message.
   *
   * @param complaint The complaint whose status was updated
   * @return EmailNotificationResponse with status and details
   */
  public EmailNotificationResponse notifyStatusUpdate(Complaint complaint) {
    String userEmail = complaint.getUser() == null ? null : complaint.getUser().getEmail();
    if (userEmail == null || userEmail.isBlank()) {
      return new EmailNotificationResponse(false, "User email is missing", "Email not sent");
    }

    try {
      String subject = "Complaint Status Update - ID #" + complaint.getId();
      String body = buildStatusUpdateEmail(complaint);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(mailFrom);
      message.setTo(userEmail);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
      log.info("Status update email sent for complaint {} to {}", complaint.getId(), userEmail);
      return new EmailNotificationResponse(true, "Email sent successfully", "Status update notification delivered");
    } catch (MailException exception) {
      log.warn("Email notification failed for complaint {}: {}", complaint.getId(), exception.getMessage());
      return new EmailNotificationResponse(false, exception.getMessage(), "Email not sent");
    }
  }

  /**
   * Send OTP verification email during registration.
   *
   * @param email The recipient email address
   * @param otp The OTP code
   * @param expirationMinutes OTP expiration time in minutes
   * @return EmailNotificationResponse with status and details
   */
  public EmailNotificationResponse sendOtpEmail(String email, String otp, int expirationMinutes) {
    try {
      String subject = "Email Verification OTP - Complaint Tracker";
      String body = buildOtpEmail(otp, expirationMinutes);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(mailFrom);
      message.setTo(email);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
      log.info("OTP email sent to {}", email);
      return new EmailNotificationResponse(true, "OTP sent successfully", "Verification email delivered");
    } catch (MailException exception) {
      log.warn("OTP email failed for {}: {}", email, exception.getMessage());
      return new EmailNotificationResponse(false, exception.getMessage(), "Email not sent");
    }
  }

  /**
   * Build the email body for status update notification.
   */
  private String buildStatusUpdateEmail(Complaint complaint) {
    return "Dear " + complaint.getUser().getFullName() + ",\n\n" +
        "We are writing to notify you that your complaint has been updated.\n\n" +
        "Complaint Details:\n" +
        "- Complaint ID: #" + complaint.getId() + "\n" +
        "- Title: " + complaint.getTitle() + "\n" +
        "- Current Status: " + complaint.getStatus().name() + "\n" +
        "- Updated On: " + complaint.getUpdatedAt() + "\n\n" +
        "Your complaint is being actively monitored and will be resolved at the earliest.\n\n" +
        "Best regards,\n" +
        mailFromName + "\n" +
        "Support Team";
  }

  /**
   * Build the email body for OTP verification.
   */
  private String buildOtpEmail(String otp, int expirationMinutes) {
    return "Dear User,\n\n" +
        "Thank you for registering with Complaint Tracker.\n\n" +
        "Your OTP for email verification is: " + otp + "\n" +
        "This OTP will expire in " + expirationMinutes + " minutes.\n\n" +
        "If you did not request this OTP, please ignore this email.\n\n" +
        "Best regards,\n" +
        mailFromName + "\n" +
        "Support Team";
  }
}

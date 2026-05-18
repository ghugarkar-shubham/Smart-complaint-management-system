package com.complainttracker.exception;

public class SmsNotificationException extends RuntimeException {
  public SmsNotificationException(String message) {
    super(message);
  }

  public SmsNotificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
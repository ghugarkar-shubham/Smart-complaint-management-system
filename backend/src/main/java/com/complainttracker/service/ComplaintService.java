package com.complainttracker.service;

import com.complainttracker.dto.ComplaintRequests.ComplaintRequest;
import com.complainttracker.dto.ComplaintRequests.StatusUpdateRequest;
import com.complainttracker.dto.Responses.ComplaintResponse;
import com.complainttracker.dto.Responses.ComplaintStatusUpdateResponse;
import com.complainttracker.dto.Responses.EmailNotificationResponse;
import com.complainttracker.exception.BadRequestException;
import com.complainttracker.exception.ResourceNotFoundException;
import com.complainttracker.model.Complaint;
import com.complainttracker.model.ComplaintStatus;
import com.complainttracker.model.User;
import com.complainttracker.repository.ComplaintRepository;
import com.complainttracker.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Service to manage complaint operations.
 * Handles complaint submission, status updates, and email notifications.
 */
@Service
public class ComplaintService {
  private final ComplaintRepository complaintRepository;
  private final UserRepository userRepository;
  private final EmailNotificationService emailNotificationService;

  public ComplaintService(ComplaintRepository complaintRepository,
                         UserRepository userRepository,
                         EmailNotificationService emailNotificationService) {
    this.complaintRepository = complaintRepository;
    this.userRepository = userRepository;
    this.emailNotificationService = emailNotificationService;
  }

  /**
   * Submit a new complaint by authenticated user.
   *
   * @param authentication The authenticated user
   * @param request ComplaintRequest with complaint details
   * @param photo Optional photo file attachment
   * @return ComplaintResponse with created complaint details
   */
  public ComplaintResponse submitComplaint(Authentication authentication,
                                          ComplaintRequest request,
                                          org.springframework.web.multipart.MultipartFile photo) {
    User user = getCurrentUser(authentication.getName());
    Complaint complaint = new Complaint();
    complaint.setTitle(request.title());
    complaint.setCategory(request.category());
    complaint.setDescription(request.description());
    complaint.setUser(user);
    complaint.setStatus(ComplaintStatus.OPEN);
    
    // Handle photo upload if present
    if (photo != null && !photo.isEmpty()) {
      try {
        java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadDir)) java.nio.file.Files.createDirectories(uploadDir);
        String original = org.springframework.util.StringUtils.cleanPath(photo.getOriginalFilename());
        String filename = System.currentTimeMillis() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        java.nio.file.Path target = uploadDir.resolve(filename);
        try (java.io.InputStream in = photo.getInputStream()) {
          java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        complaint.setPhotoFilename(filename);
      } catch (java.io.IOException e) {
        throw new BadRequestException("Failed to store uploaded file");
      }
    }
    complaintRepository.save(complaint);
    return mapComplaint(complaint);
  }

  /**
   * Get all complaints submitted by authenticated user.
   *
   * @param authentication The authenticated user
   * @return List of user's complaints
   */
  public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
    User user = getCurrentUser(authentication.getName());
    return complaintRepository.findByUserOrderByCreatedAtDesc(user)
        .stream()
        .map(this::mapComplaint)
        .toList();
  }

  /**
   * Get single complaint by ID with user authorization check.
   *
   * @param authentication The authenticated user
   * @param id Complaint ID
   * @return ComplaintResponse with complaint details
   * @throws ResourceNotFoundException if complaint not found
   * @throws BadRequestException if user is not the owner
   */
  public ComplaintResponse getOne(Authentication authentication, @NonNull Long id) {
    User user = getCurrentUser(authentication.getName());
    Complaint complaint = complaintRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    if (!complaint.getUser().getId().equals(user.getId())) {
      throw new BadRequestException("You cannot access this complaint");
    }
    return mapComplaint(complaint);
  }

  /**
   * Get complaints for admin view with optional search and status filter.
   *
   * @param search Optional search text
   * @param status Optional status filter
   * @return List of complaints matching criteria
   */
  public List<ComplaintResponse> getAdminComplaints(String search, String status) {
    List<Complaint> complaints;
    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
      ComplaintStatus complaintStatus = ComplaintStatus.valueOf(status.toUpperCase(Locale.ROOT));
      complaints = search == null || search.isBlank()
          ? complaintRepository.findByStatusOrderByCreatedAtDesc(complaintStatus)
          : complaintRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(complaintStatus, search);
    } else if (search != null && !search.isBlank()) {
      complaints = complaintRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(search, search, search);
    } else {
      complaints = complaintRepository.findAll()
          .stream()
          .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
          .toList();
    }
    return complaints.stream().map(this::mapComplaint).toList();
  }

  /**
   * Update complaint status and send email notification to user.
   * Called by admin when updating complaint status.
   *
   * @param id Complaint ID
   * @param request StatusUpdateRequest with new status
   * @return ComplaintStatusUpdateResponse with updated complaint and notification status
   * @throws ResourceNotFoundException if complaint not found
   * @throws BadRequestException if status is invalid
   */
  public ComplaintStatusUpdateResponse updateStatus(@NonNull Long id, StatusUpdateRequest request) {
    Complaint complaint = complaintRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    
    try {
      complaint.setStatus(ComplaintStatus.valueOf(request.status().toUpperCase(Locale.ROOT)));
    } catch (Exception exception) {
      throw new BadRequestException("Invalid status value");
    }
    
    if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
      complaint.setResolvedBy("Admin");
    }
    complaintRepository.save(complaint);

    // Send email notification after complaint status is updated
    EmailNotificationResponse emailNotification = emailNotificationService.notifyStatusUpdate(complaint);
    return new ComplaintStatusUpdateResponse(mapComplaint(complaint), emailNotification);
  }

  /**
   * Delete a complaint by ID.
   *
   * @param id Complaint ID
   * @throws ResourceNotFoundException if complaint not found
   */
  public void deleteComplaint(@NonNull Long id) {
    if (!complaintRepository.existsById(id)) {
      throw new ResourceNotFoundException("Complaint not found");
    }
    complaintRepository.deleteById(id);
  }

  /**
   * Count complaints by status.
   *
   * @param status The complaint status to filter by
   * @return Count of complaints with given status
   */
  public long countByStatus(ComplaintStatus status) {
    return complaintRepository.findByStatusOrderByCreatedAtDesc(status).size();
  }

  /**
   * Get total count of all complaints.
   *
   * @return Total complaint count
   */
  public long countAll() {
    return complaintRepository.count();
  }

  /**
   * Get photo filename for a complaint.
   *
   * @param id Complaint ID
   * @return Photo filename if exists
   * @throws ResourceNotFoundException if complaint not found
   */
  public String getPhotoFilename(Long id) {
    Complaint complaint = complaintRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    return complaint.getPhotoFilename();
  }

  private User getCurrentUser(String email) {
    return userRepository.findByEmail(email.toLowerCase())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private ComplaintResponse mapComplaint(Complaint complaint) {
    return new ComplaintResponse(
        complaint.getId(),
        complaint.getTitle(),
        complaint.getCategory(),
        complaint.getDescription(),
        complaint.getStatus().name(),
        complaint.getCreatedAt(),
        complaint.getUpdatedAt(),
        complaint.getUser().getFullName(),
        complaint.getResolvedBy(),
        complaint.getPhotoFilename());
  }
}

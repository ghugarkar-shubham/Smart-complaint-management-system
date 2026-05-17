package com.complainttracker.service;

import com.complainttracker.dto.ComplaintRequests.ComplaintRequest;
import com.complainttracker.dto.ComplaintRequests.StatusUpdateRequest;
import com.complainttracker.dto.Responses.ComplaintResponse;
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

@Service
public class ComplaintService {
  private final ComplaintRepository complaintRepository;
  private final UserRepository userRepository;

  public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository) {
    this.complaintRepository = complaintRepository;
    this.userRepository = userRepository;
  }

  public ComplaintResponse submitComplaint(Authentication authentication, ComplaintRequest request, org.springframework.web.multipart.MultipartFile photo) {
    User user = getCurrentUser(authentication.getName());
    Complaint complaint = new Complaint();
    complaint.setTitle(request.title());
    complaint.setCategory(request.category());
    complaint.setDescription(request.description());
    complaint.setUser(user);
    complaint.setStatus(ComplaintStatus.OPEN);
    // handle photo upload if present
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

  public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
    User user = getCurrentUser(authentication.getName());
    return complaintRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::mapComplaint).toList();
  }

  public ComplaintResponse getOne(Authentication authentication, @NonNull Long id) {
    User user = getCurrentUser(authentication.getName());
    Complaint complaint = complaintRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    if (!complaint.getUser().getId().equals(user.getId())) {
      throw new BadRequestException("You cannot access this complaint");
    }
    return mapComplaint(complaint);
  }

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
      complaints = complaintRepository.findAll().stream().sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt())).toList();
    }

    return complaints.stream().map(this::mapComplaint).toList();
  }

  public ComplaintResponse updateStatus(@NonNull Long id, StatusUpdateRequest request) {
    Complaint complaint = complaintRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    try {
      complaint.setStatus(ComplaintStatus.valueOf(request.status().toUpperCase(Locale.ROOT)));
    } catch (Exception exception) {
      throw new BadRequestException("Invalid status value");
    }
    if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
      complaint.setResolvedBy("Admin");
    }
    complaintRepository.save(complaint);
    return mapComplaint(complaint);
  }

  public void deleteComplaint(@NonNull Long id) {
    if (!complaintRepository.existsById(id)) {
      throw new ResourceNotFoundException("Complaint not found");
    }
    complaintRepository.deleteById(id);
  }

  public long countByStatus(ComplaintStatus status) {
    return complaintRepository.findByStatusOrderByCreatedAtDesc(status).size();
  }

  public long countAll() {
    return complaintRepository.count();
  }

  private User getCurrentUser(String email) {
    return userRepository.findByEmail(email.toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

  public String getPhotoFilename(Long id) {
    Complaint complaint = complaintRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    return complaint.getPhotoFilename();
  }
}

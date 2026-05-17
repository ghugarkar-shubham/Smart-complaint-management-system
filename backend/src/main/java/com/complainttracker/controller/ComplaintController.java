package com.complainttracker.controller;

import com.complainttracker.dto.ComplaintRequests.ComplaintRequest;
import com.complainttracker.dto.Responses.ComplaintResponse;
import com.complainttracker.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.UrlResource;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
  private final ComplaintService complaintService;

  public ComplaintController(ComplaintService complaintService) {
    this.complaintService = complaintService;
  }

  @GetMapping
  public ResponseEntity<List<ComplaintResponse>> mine(Authentication authentication) {
    return ResponseEntity.ok(complaintService.getMyComplaints(authentication));
  }

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<ComplaintResponse> create(Authentication authentication,
                                                  @Valid @RequestPart("data") ComplaintRequest request,
                                                  @RequestPart(value = "photo", required = false) MultipartFile photo) {
    return ResponseEntity.ok(complaintService.submitComplaint(authentication, request, photo));
  }

  @GetMapping(path = "/{id}/photo")
  public ResponseEntity<Resource> getPhoto(Authentication authentication, @PathVariable @NonNull Long id) throws Exception {
    // ensure user can access
    complaintService.getOne(authentication, id);
    String filename = complaintService.getPhotoFilename(id);
    if (filename == null) return ResponseEntity.notFound().build();
    java.nio.file.Path file = java.nio.file.Paths.get("uploads").resolve(filename).normalize();
    Resource resource = new UrlResource(file.toUri());
    if (!resource.exists()) return ResponseEntity.notFound().build();
    String contentType = java.nio.file.Files.probeContentType(file);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType == null ? "application/octet-stream" : contentType)).body(resource);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ComplaintResponse> getOne(Authentication authentication, @PathVariable @NonNull Long id) {
    return ResponseEntity.ok(complaintService.getOne(authentication, id));
  }
}

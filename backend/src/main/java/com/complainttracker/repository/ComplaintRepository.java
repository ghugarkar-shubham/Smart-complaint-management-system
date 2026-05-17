package com.complainttracker.repository;

import com.complainttracker.model.Complaint;
import com.complainttracker.model.ComplaintStatus;
import com.complainttracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
  List<Complaint> findByUserOrderByCreatedAtDesc(User user);
  List<Complaint> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String title, String category, String description);
  List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);
  List<Complaint> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(ComplaintStatus status, String title);
}

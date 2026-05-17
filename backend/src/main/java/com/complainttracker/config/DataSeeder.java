package com.complainttracker.config;

import com.complainttracker.model.Complaint;
import com.complainttracker.model.ComplaintStatus;
import com.complainttracker.model.Role;
import com.complainttracker.model.User;
import com.complainttracker.repository.ComplaintRepository;
import com.complainttracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
  @Bean
  public CommandLineRunner seed(UserRepository userRepository, ComplaintRepository complaintRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      User admin = userRepository.findByEmail("admin@complaints.com").orElseGet(() -> {
        User user = new User();
        user.setFullName("System Admin");
        user.setEmail("admin@complaints.com");
        user.setPassword(passwordEncoder.encode("Admin@12345"));
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
      });

      User user = userRepository.findByEmail("user@complaints.com").orElseGet(() -> {
        User demo = new User();
        demo.setFullName("Demo User");
        demo.setEmail("user@complaints.com");
        demo.setPassword(passwordEncoder.encode("User@12345"));
        demo.setRole(Role.USER);
        return userRepository.save(demo);
      });

      if (complaintRepository.count() == 0) {
        Complaint first = new Complaint();
        first.setTitle("Street light not working");
        first.setCategory("Infrastructure");
        first.setDescription("The street light near block A has been off for a week.");
        first.setStatus(ComplaintStatus.OPEN);
        first.setUser(user);
        complaintRepository.save(first);

        Complaint second = new Complaint();
        second.setTitle("Water leakage in corridor");
        second.setCategory("Facility");
        second.setDescription("Persistent water leakage has caused slippery floors.");
        second.setStatus(ComplaintStatus.IN_PROGRESS);
        second.setResolvedBy(admin.getFullName());
        second.setUser(user);
        complaintRepository.save(second);

        Complaint third = new Complaint();
        third.setTitle("Delayed waste collection");
        third.setCategory("Sanitation");
        third.setDescription("Garbage collection is delayed every alternate day.");
        third.setStatus(ComplaintStatus.RESOLVED);
        third.setResolvedBy("Municipal Team");
        third.setUser(user);
        complaintRepository.save(third);
      }
    };
  }
}

package com.complainttracker.service;

import com.complainttracker.dto.AuthRequests.LoginRequest;
import com.complainttracker.dto.AuthRequests.RegisterRequest;
import com.complainttracker.dto.Responses.AuthResponse;
import com.complainttracker.exception.BadRequestException;
import com.complainttracker.model.Role;
import com.complainttracker.model.User;
import com.complainttracker.repository.UserRepository;
import com.complainttracker.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email().toLowerCase())) {
      throw new BadRequestException("Email already registered");
    }

    User user = new User();
    user.setFullName(request.fullName());
    user.setEmail(request.email().toLowerCase());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER);
    userRepository.save(user);
  }

  public AuthResponse login(LoginRequest request) {
    String email = request.email().toLowerCase();
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
    User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Invalid credentials"));
    return new AuthResponse(jwtService.generateToken(user), user.getRole().name(), user.getFullName(), user.getEmail());
  }
}

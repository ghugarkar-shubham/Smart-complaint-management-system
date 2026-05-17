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

/**
 * Service to handle user authentication and registration.
 * Manages login, registration, and email verification flow.
 */
@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final EmailOtpVerificationService emailOtpVerificationService;

  public AuthService(UserRepository userRepository,
                    PasswordEncoder passwordEncoder,
                    AuthenticationManager authenticationManager,
                    JwtService jwtService,
                    EmailOtpVerificationService emailOtpVerificationService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.emailOtpVerificationService = emailOtpVerificationService;
  }

  /**
   * Register a new user after email OTP verification.
   * Email must be verified via OTP before registration is allowed.
   *
   * @param request RegisterRequest containing fullName, email, and password
   * @throws BadRequestException if email is already registered or not verified
   */
  public void register(RegisterRequest request) {
    String email = request.email().toLowerCase().trim();
    
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Email already registered");
    }

    // Verify that email has been verified via OTP
    emailOtpVerificationService.requireVerifiedEmail(email);

    // Create new user
    User user = new User();
    user.setFullName(request.fullName().trim());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER);
    user.setEmailVerified(true);
    userRepository.save(user);

    // Mark OTP as consumed to prevent reuse
    emailOtpVerificationService.consumeVerifiedEmail(email);
  }

  /**
   * Authenticate user with email and password.
   * Returns JWT token for authenticated user.
   *
   * @param request LoginRequest containing email and password
   * @return AuthResponse with JWT token and user details
   * @throws BadRequestException if credentials are invalid
   */
  public AuthResponse login(LoginRequest request) {
    String email = request.email().toLowerCase().trim();
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadRequestException("Invalid credentials"));
    return new AuthResponse(jwtService.generateToken(user), user.getRole().name(), user.getFullName(), user.getEmail());
  }
}

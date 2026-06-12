package com.microlend.service;

import com.microlend.dto.request.LoginRequest;
import com.microlend.dto.request.RegisterUserRequest;
import com.microlend.dto.response.AuthResponse;
import com.microlend.entity.User;
import com.microlend.enums.UserRole;
import com.microlend.exception.BadRequestException;
import com.microlend.repository.UserRepository;
import com.microlend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AuthService
 *
 * BUG FIX #1: register() is called only by authenticated callers (ADMIN or
 *             BRANCH_MANAGER), enforced by @PreAuthorize in AuthController.
 *
 * BUG FIX #2: BRANCH_MANAGER scope enforcement inside register().
 *   - BMs may only create: FIELD_OFFICER, CREDIT_OFFICER, COLLECTIONS_OFFICER.
 *   - BMs cannot create ADMIN or BRANCH_MANAGER (no privilege escalation).
 *   - BMs can only register users into their own branchID.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /** Roles a BRANCH_MANAGER is permitted to provision. */
    private static final List<UserRole> MANAGER_PROVISIONABLE_ROLES = List.of(
            UserRole.FIELD_OFFICER,
            UserRole.CREDIT_OFFICER,
            UserRole.COLLECTIONS_OFFICER
    );

    /**
     * Register a new staff user.
     * Caller must be ADMIN or BRANCH_MANAGER (enforced in AuthController).
     * BRANCH_MANAGER scope is additionally enforced here.
     */
    public AuthResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // BUG FIX #2: Enforce BRANCH_MANAGER scope restrictions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BRANCH_MANAGER"));

        if (isManager) {
            validateBranchManagerProvisioning(auth, request);
        }

        User user = User.builder()
                .name(request.getName())
                .role(request.getRole())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .branchID(request.getBranchID())
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved);

        return AuthResponse.builder()
                .token(token)
                .userID(saved.getUserID())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .branchID(saved.getBranchID())
                .build();
    }

    /**
     * BUG FIX #2: Scope check for BRANCH_MANAGER registration requests.
     * Prevents privilege escalation and cross-branch user creation.
     */
    private void validateBranchManagerProvisioning(Authentication auth,
                                                    RegisterUserRequest request) {
        // Role restriction: BM cannot create ADMIN or another BRANCH_MANAGER
        if (!MANAGER_PROVISIONABLE_ROLES.contains(request.getRole())) {
            throw new SecurityException(
                    "BRANCH_MANAGER is not authorized to create role: " + request.getRole() +
                    ". Allowed roles: " + MANAGER_PROVISIONABLE_ROLES);
        }

        // Branch restriction: BM can only add users to their own branch
        User caller = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));

        if (caller.getBranchID() == null) {
            throw new SecurityException("BRANCH_MANAGER has no branch assignment.");
        }
        if (!caller.getBranchID().equals(request.getBranchID())) {
            throw new SecurityException(
                    "BRANCH_MANAGER can only provision users within their own branch (ID: "
                    + caller.getBranchID() + "). Requested branch: " + request.getBranchID());
        }
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userID(user.getUserID())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .branchID(user.getBranchID())
                .build();
    }
}

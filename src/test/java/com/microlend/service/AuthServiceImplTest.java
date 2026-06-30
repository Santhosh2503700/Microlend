package com.microlend.service;

import com.microlend.dto.request.LoginRequest;
import com.microlend.dto.request.RegisterUserRequest;
import com.microlend.dto.response.AuthResponse;
import com.microlend.entity.User;
import com.microlend.enums.UserRole;
import com.microlend.exception.BadRequestException;
import com.microlend.repository.UserRepository;
import com.microlend.security.JwtUtil;
import com.microlend.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests — Fixed Version")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;
    private RegisterUserRequest fieldOfficerRequest;
    private User adminUser;
    private User branchManagerUser;

    @BeforeEach
    void setUp() {
        fieldOfficerRequest = new RegisterUserRequest();
        fieldOfficerRequest.setName("Ravi Kumar");
        fieldOfficerRequest.setRole(UserRole.FIELD_OFFICER);
        fieldOfficerRequest.setEmail("ravi@microlend.com");
        fieldOfficerRequest.setPassword("Field@123");
        fieldOfficerRequest.setPhone("9876500002");
        fieldOfficerRequest.setBranchID(1L);

        adminUser = User.builder()
                .userID(1L).name("Admin").email("admin@microlend.com")
                .role(UserRole.ADMIN).branchID(1L).build();

        branchManagerUser = User.builder()
                .userID(2L).name("Anita Singh").email("anita@microlend.com")
                .role(UserRole.BRANCH_MANAGER).branchID(1L).build();
    }

    private void setSecurityContextAs(UserRole role, String email) {
        Authentication auth = mock(Authentication.class);

        lenient().when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));

        lenient().when(auth.getName()).thenReturn(email);

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        setSecurityContextAs(UserRole.ADMIN, "admin@microlend.com");
        when(userRepository.existsByEmail("ravi@microlend.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(fieldOfficerRequest))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_adminCanRegister_success() {
        setSecurityContextAs(UserRole.ADMIN, "admin@microlend.com");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        User saved = User.builder()
                .userID(4L)
                .role(UserRole.FIELD_OFFICER)
                .build();

        when(userRepository.save(any())).thenReturn(saved);
        when(jwtUtil.generateToken(any())).thenReturn("mock.jwt.token");

        AuthResponse result = authService.register(fieldOfficerRequest);

        assertThat(result.getUserID()).isEqualTo(4L);
        assertThat(result.getToken()).isEqualTo("mock.jwt.token");
    }

    @Test
    void register_branchManager_success() {
        setSecurityContextAs(UserRole.BRANCH_MANAGER, "anita@microlend.com");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.findByEmail("anita@microlend.com"))
                .thenReturn(Optional.of(branchManagerUser));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(branchManagerUser);
        when(jwtUtil.generateToken(any())).thenReturn("token");

        AuthResponse result = authService.register(fieldOfficerRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void register_branchManagerCannotCreateAdmin() {

        fieldOfficerRequest.setRole(UserRole.ADMIN);

        setSecurityContextAs(UserRole.BRANCH_MANAGER, "anita@microlend.com");

        lenient().when(userRepository.findByEmail("anita@microlend.com"))
                .thenReturn(Optional.of(branchManagerUser));

        assertThatThrownBy(() -> authService.register(fieldOfficerRequest))
                .isInstanceOf(SecurityException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_crossBranch_throwsException() {
        fieldOfficerRequest.setBranchID(99L);

        setSecurityContextAs(UserRole.BRANCH_MANAGER, "anita@microlend.com");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.findByEmail("anita@microlend.com"))
                .thenReturn(Optional.of(branchManagerUser));

        assertThatThrownBy(() -> authService.register(fieldOfficerRequest))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("another branch");
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@microlend.com");
        req.setPassword("admin123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("admin@microlend.com"))
                .thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken(adminUser))
                .thenReturn("token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    void login_userNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@microlend.com");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("ghost@microlend.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class);
    }
}
package com.microlend.config;

import com.microlend.entity.User;
import com.microlend.enums.UserRole;
import com.microlend.enums.UserStatus;
import com.microlend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("Admin User",       UserRole.ADMIN,               "admin@microlend.com",       "admin123",    1L);
        seedUser("Credit Officer",   UserRole.CREDIT_OFFICER,      "credit@microlend.com",      "credit123",   1L);
        seedUser("Field Officer",    UserRole.FIELD_OFFICER,       "field@microlend.com",       "field123",    1L);
        seedUser("Branch Manager",   UserRole.BRANCH_MANAGER,      "branch@microlend.com",      "branch123",   1L);
        seedUser("Collections",      UserRole.COLLECTIONS_OFFICER, "collections@microlend.com", "collect123",  1L);
        seedUser("Borrower One",     UserRole.BORROWER,            "borrower@microlend.com",    "borrower123", null);

        log.info("========================================");
        log.info("  MicroLend Default Users Seeded");
        log.info("  Admin:       admin@microlend.com / admin123");
        log.info("  Credit Off:  credit@microlend.com / credit123");
        log.info("  Field Off:   field@microlend.com / field123");
        log.info("  Branch Mgr:  branch@microlend.com / branch123");
        log.info("  Collections: collections@microlend.com / collect123");
        log.info("  Borrower:    borrower@microlend.com / borrower123");
        log.info("========================================");
    }

    private void seedUser(String name, UserRole role, String email, String password, Long branchID) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .name(name)
                    .role(role)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .branchID(branchID)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
            log.info("Seeded user: {} ({})", email, role);
        }
    }
}

package com.microlend.config;

import com.microlend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — MicroLend Phase 1 (Bug-Fixed)
 *
 * BUG FIX #1: /api/auth/register is NO LONGER in permitAll().
 *   Previously: .requestMatchers("/api/auth/**").permitAll() gave anonymous
 *               callers full access to register any role including ADMIN.
 *   Fixed:      Only /api/auth/login is public. /api/auth/register now
 *               requires ADMIN or BRANCH_MANAGER authentication.
 *               Anonymous → 401 Unauthorized. Wrong role → 403 Forbidden.
 *
 * BUG FIX #2: BRANCH_MANAGER can now access /api/auth/register.
 *   Previously: The blanket ADMIN-only rule on /api/admin/** blocked
 *               BRANCH_MANAGER from provisioning staff accounts.
 *   Fixed:      POST /api/auth/register allows ADMIN + BRANCH_MANAGER.
 *               Scope (branch + role) is enforced inside AuthService.
 *
 * BUG FIX #3: FIELD_OFFICER wildcard on /api/kyc/** is removed.
 *   Previously: .requestMatchers("/api/kyc/**").hasAnyRole("ADMIN","CREDIT_OFFICER","FIELD_OFFICER")
 *               gave FIELD_OFFICER access to PATCH /api/kyc/{id}/verify — same
 *               person who uploaded docs could self-verify (maker-checker violation).
 *   Fixed:      FIELD_OFFICER only reaches POST /api/kyc (document upload).
 *               PATCH /api/kyc/{id}/verify is restricted to ADMIN + CREDIT_OFFICER.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC: login only ──────────────────────────────────────
                // BUG FIX #1: /api/auth/register is intentionally NOT here.
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // ── ADMIN-ONLY ──────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── USER REGISTRATION ───────────────────────────────────────
                // BUG FIX #1 + #2: Register requires auth; BRANCH_MANAGER allowed.
                // Scope enforcement (branch + provisionable roles) is in AuthService.
                .requestMatchers(HttpMethod.POST, "/api/auth/register")
                        .hasAnyRole("ADMIN", "BRANCH_MANAGER")

                // ── LOAN PRODUCTS ───────────────────────────────────────────
                .requestMatchers("/api/loan-products/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER")

                // ── BORROWER MANAGEMENT ─────────────────────────────────────
                .requestMatchers("/api/borrowers/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "FIELD_OFFICER", "BRANCH_MANAGER")

                // ── KYC — SPLIT RULES (BUG FIX #3) ─────────────────────────
                // FIELD_OFFICER may ONLY upload docs (POST /api/kyc).
                // PATCH /api/kyc/{id}/verify is ADMIN + CREDIT_OFFICER only.
                // The old wildcard .requestMatchers("/api/kyc/**").hasAnyRole(...FIELD_OFFICER)
                // is replaced with explicit, ordered rules.
                .requestMatchers(HttpMethod.POST, "/api/kyc")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "FIELD_OFFICER")
                .requestMatchers(HttpMethod.PATCH, "/api/kyc/{id}/verify")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER")
                .requestMatchers("/api/kyc/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "BRANCH_MANAGER")

                // ── CREDIT ASSESSMENTS ──────────────────────────────────────
                .requestMatchers("/api/credit-assessments/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER")

                // ── CENTRES & MEETINGS ──────────────────────────────────────
                .requestMatchers("/api/centres/**")
                        .hasAnyRole("ADMIN", "FIELD_OFFICER", "BRANCH_MANAGER")
                .requestMatchers("/api/groups/**")
                        .hasAnyRole("ADMIN", "FIELD_OFFICER", "BRANCH_MANAGER")
                .requestMatchers("/api/meetings/**")
                        .hasAnyRole("ADMIN", "FIELD_OFFICER", "BRANCH_MANAGER")

                // ── LOAN LIFECYCLE ──────────────────────────────────────────
                .requestMatchers("/api/loan-applications/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "BORROWER", "BRANCH_MANAGER", "FIELD_OFFICER")
                .requestMatchers("/api/sanction-letters/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "BORROWER")
                .requestMatchers("/api/loan-accounts/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "BORROWER", "BRANCH_MANAGER", "COLLECTIONS_OFFICER")
                .requestMatchers("/api/repayment-schedules/**")
                        .hasAnyRole("ADMIN", "CREDIT_OFFICER", "BORROWER", "FIELD_OFFICER")

                // ── COLLECTIONS & DELINQUENCY ───────────────────────────────
                .requestMatchers("/api/collections/**")
                        .hasAnyRole("ADMIN", "FIELD_OFFICER", "COLLECTIONS_OFFICER", "BRANCH_MANAGER")
                .requestMatchers("/api/delinquency/**")
                        .hasAnyRole("ADMIN", "COLLECTIONS_OFFICER", "BRANCH_MANAGER")

                // ── REPORTS ─────────────────────────────────────────────────
                .requestMatchers("/api/reports/**")
                        .hasAnyRole("ADMIN", "BRANCH_MANAGER")

                // ── NOTIFICATIONS ────────────────────────────────────────────
                .requestMatchers("/api/notifications/**").authenticated()

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

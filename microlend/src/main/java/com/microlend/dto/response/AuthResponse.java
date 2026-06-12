package com.microlend.dto.response;

import com.microlend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userID;
    private String name;
    private String email;
    private UserRole role;
    private Long branchID;
}

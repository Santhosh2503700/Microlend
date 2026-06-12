package com.microlend.dto.request;

import com.microlend.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotBlank private String name;
    @NotNull private UserRole role;
    @NotBlank @Email private String email;
    @NotBlank private String password;
    private String phone;
    private Long branchID;
}

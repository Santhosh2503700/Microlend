package com.microlend.dto.request;

import com.microlend.enums.BorrowerStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BorrowerRequest {
    @NotBlank private String name;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationalIDNumber;
    private String village;
    private String district;
    private String phone;
    private String occupation;
    private BigDecimal monthlyIncome;
    private String bankAccountNumber;
    private BorrowerStatus status;
}

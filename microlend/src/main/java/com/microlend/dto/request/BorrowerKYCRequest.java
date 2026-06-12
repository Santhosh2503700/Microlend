package com.microlend.dto.request;

import com.microlend.enums.KYCDocumentType;
import com.microlend.enums.KYCStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowerKYCRequest {
    @NotNull private Long borrowerID;
    @NotNull private KYCDocumentType documentType;
    private String documentRef;
    private Long verifiedByID;
    private LocalDate verificationDate;
    private KYCStatus status;
}

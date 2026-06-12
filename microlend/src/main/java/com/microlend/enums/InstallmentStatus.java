package com.microlend.enums;

/**
 * InstallmentStatus
 *
 * BUG FIX #4: Added PARTIAL to represent installments where the borrower
 * has made a payment that covers some but not all of the amount due.
 * Previously, partial payments left the status as PENDING, hiding the fact
 * that progress had been made on the installment.
 */
public enum InstallmentStatus {
    PENDING,
    PARTIAL,    // BUG FIX #4: partial payment received, installment not yet cleared
    PAID,
    OVERDUE,
    WAIVED
}

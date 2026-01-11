package com.example.genggamin.enums;

public enum NotificationType {
    // Customer Events
    REGISTER,
    FORGOT_PASSWORD,
    LOAN_SUBMISSION,
    LOAN_APPROVED,
    LOAN_REJECTED,
    LOAN_DISBURSED,

    // Staff Events
    LOAN_NEW,           // For Marketing
    REVIEW_COMPLETED,   // For Marketing
    READY_FOR_APPROVAL, // For Branch Manager
    APPROVAL_COMPLETED, // For Branch Manager
    READY_FOR_DISBURSEMENT, // For Backoffice
    DISBURSEMENT_COMPLETED  // For Backoffice
}

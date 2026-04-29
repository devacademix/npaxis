package com.digitalearn.npaxis.admin.dto;

import java.time.LocalDateTime;

/**
 * DTO for transaction history
 */
public record TransactionHistoryDTO(
        String transactionId,
        Long userId,
        String displayName,
        String transactionType, // "PAYMENT", "REFUND", "INVOICE", "CHARGEBACK"
        Double amount,
        String status, // "SUCCEEDED", "FAILED", "PENDING"
        String paymentMethod,
        LocalDateTime transactionDate
) {
}


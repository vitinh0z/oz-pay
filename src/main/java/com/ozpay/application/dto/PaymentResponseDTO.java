package com.ozpay.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponseDTO(

        UUID paymentId,

        String status,

        BigDecimal amount,

        String currency,

        GatewayDetails details,

        LocalDateTime createAt

) {}



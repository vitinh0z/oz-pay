package com.ozpay.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentRequestDTO(

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "A moeda é obrigatoria")
        @Pattern(regexp = "[A-Z]{3}", message = "A Moeda deve ser no padrão ISO (BRL, USD, EUR)")
        String currency,

        @Valid
        @NotNull(message = "Deve conter informações de pagamento")
        PaymentMethodRequest paymentMethodRequest,

        @Valid
        @NotNull(message = "Deve conter informação do cliente")
        CustomerRequest customer,

        Map<String, String> metadata
) {}

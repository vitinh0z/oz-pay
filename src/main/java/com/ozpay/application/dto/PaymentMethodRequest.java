package com.ozpay.application.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentMethodRequest(

        @NotNull(message = "Tipo de pagamento é obrigatório")
        String type, // boleto, pix, card

        String token


) {
}

package com.ozpay.infra.persistence.mapper;

import com.ozpay.application.dto.PaymentRequestDTO;
import com.ozpay.application.dto.PaymentResponseDTO;
import com.ozpay.domain.entity.Payment;
import com.ozpay.domain.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentRequestDtoMapper {

    public Payment toDomain (PaymentRequestDTO dto){
        if (dto == null) return null;

        return new Payment(
                null,
                null,
                dto.amount(),
                PaymentStatus.PENDING,
                dto.currency(),
                dto.paymentMethodRequest().token(),
                LocalDateTime.now()

        );
    }

    public PaymentResponseDTO toDto (Payment domain){
        if (domain == null) return null;

        return new PaymentResponseDTO(
                domain.getId(),
                domain.getStatus().toString(),
                domain.getMoney(),
                domain.getCurrency(),
                null,
                LocalDateTime.now()

        );


    }

}

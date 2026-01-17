package com.ozpay.application.usecase;

import com.ozpay.application.dto.PaymentRequestDTO;
import com.ozpay.application.dto.PaymentResponseDTO;
import com.ozpay.domain.entity.Payment;
import com.ozpay.domain.repository.payment.PaymentRepository;
import com.ozpay.infra.gateway.PaymentGateway;
import com.ozpay.infra.persistence.mapper.PaymentRequestDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessPaymentUsaCase {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentRequestDtoMapper paymentMapper;


    public PaymentResponseDTO execute(PaymentRequestDTO payment){

        Payment domain = paymentMapper.toDomain(payment);
        Payment processPayment = paymentGateway.process(domain);
        Payment savePayment = paymentRepository.save(processPayment);
        return paymentMapper.toDto(savePayment);
    }

}

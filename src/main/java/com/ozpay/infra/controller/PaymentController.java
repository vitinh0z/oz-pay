package com.ozpay.infra.controller;

import com.ozpay.application.dto.PaymentRequestDTO;
import com.ozpay.application.dto.PaymentResponseDTO;
import com.ozpay.application.usecase.ProcessPaymentUsaCase;
import com.ozpay.domain.entity.Payment;
import com.ozpay.infra.persistence.PaymentPersistenceGateway;
import com.ozpay.infra.persistence.mapper.PaymentMapper;
import com.ozpay.infra.persistence.mapper.PaymentRequestDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(name ="/v1/")
@RequiredArgsConstructor
public class PaymentController {

    private final ProcessPaymentUsaCase usecase;


    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDTO> payment (@Valid @RequestBody PaymentRequestDTO dto,
                                                       UriComponentsBuilder uri
    ){
        PaymentResponseDTO response = usecase.execute(dto);

        URI url = uri.path("/v1/payments/{id}")
                .buildAndExpand(response.paymentId())
                .toUri();

        return ResponseEntity.created(url).body(response);
    }

}

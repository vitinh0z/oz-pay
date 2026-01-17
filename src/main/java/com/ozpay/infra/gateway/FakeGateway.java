package com.ozpay.infra.gateway;

import com.ozpay.domain.entity.Payment;
import com.ozpay.domain.entity.PaymentStatus;

import java.util.UUID;

public class FakeGateway implements PaymentGateway{

    @Override
    public Payment process(Payment payment) {

        try {
            Thread.sleep(300); // Simulamos latência de 3ms
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(payment.getMoney().doubleValue() == 99.99){
            payment.setStatus(PaymentStatus.ERROR);
        } // apenas para teste

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setGatewayTransactionId("sk_" + UUID.randomUUID());

        return payment;
    }
}

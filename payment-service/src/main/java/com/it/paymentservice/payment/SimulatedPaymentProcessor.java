package com.it.paymentservice.payment;

import com.it.paymentservice.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SimulatedPaymentProcessor implements PaymentProcessor {

    private static final BigDecimal SUCCESS_THRESHOLD = new BigDecimal("10000.00");

    @Override
    public PaymentStatus processPayment(BigDecimal importo){

        if (importo.compareTo(SUCCESS_THRESHOLD) > 0){
            return PaymentStatus.FAILED;
        }

        return PaymentStatus.SUCCESS;
    }

}

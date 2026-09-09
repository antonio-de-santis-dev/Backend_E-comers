package com.it.paymentservice.payment;

import com.it.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentProcessor {

    PaymentStatus processPayment(BigDecimal importo);


}

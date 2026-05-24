package com.example.demo.payment;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


public record PaymentRequestDto(

        @NotNull
        PaymentMethod paymentMethod,

        String provider,

     

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount

) {
}

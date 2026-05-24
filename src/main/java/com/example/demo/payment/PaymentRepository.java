package com.example.demo.payment;

import java.util.List;

public interface  PaymentRepository extends org.springframework.data.jpa.repository.JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);

}

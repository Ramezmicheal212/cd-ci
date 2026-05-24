package com.example.demo.Booking.servicesBooking;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Simple cancellation policy rules:
 *
 *  
 *  Days until check-in              │ Refund     
 *  
 *  │ > 7 days                         │ 100%         
 *  │ 3 – 7 days (inclusive)           │  50%         
 *  │ < 3 days                         │   0%        
 *  
 */
@Service
public class CancellationPolicyService {

    public record PolicyResult(int refundPercentage, String description) {}

    /**
     * Evaluate the refund entitlement based on today vs check-in date.
     */
    public PolicyResult evaluate(LocalDate checkInDate) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);

        if (daysUntilCheckIn > 7) {
            return new PolicyResult(100, "Full refund – cancellation more than 7 days before check-in");
        } else if (daysUntilCheckIn >= 3) {
            return new PolicyResult(50, "50% refund – cancellation 3–7 days before check-in");
        } else {
            return new PolicyResult(0, "No refund – cancellation less than 3 days before check-in");
        }
    }

    /**
     * Convenience: return just the human-readable label for display in responses.
     */
    public String policyLabel(LocalDate checkInDate) {
        return evaluate(checkInDate).description();
    }
}
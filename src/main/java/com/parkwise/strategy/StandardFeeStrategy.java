package com.parkwise.strategy;

import com.parkwise.model.Tariff;

import java.math.BigDecimal;

/**
 * Standard fee calculation strategy.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>A session lasting up to 60 minutes → first-hour charge only.</li>
 *   <li>Each additional <em>started</em> minute block beyond the first hour
 *       counts as a complete additional hour.</li>
 * </ul>
 *
 * <p>Example (four-wheeler, ₹40/hr + ₹20/add. hr):</p>
 * <ul>
 *   <li>45 min → ₹40</li>
 *   <li>60 min → ₹40</li>
 *   <li>61 min → ₹60  (1 additional started hour)</li>
 *   <li>120 min → ₹60</li>
 *   <li>121 min → ₹80</li>
 * </ul>
 */
public class StandardFeeStrategy implements ParkingFeeStrategy {

    @Override
    public BigDecimal calculate(int durationMinutes, Tariff tariff) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + durationMinutes);
        }

        // Autoboxing: int → Integer used via Math.max
        int effectiveMinutes = Math.max(durationMinutes, 1);  // minimum 1 minute

        BigDecimal fee = tariff.getFirstHourFee();

        if (effectiveMinutes > 60) {
            // Integer arithmetic for ceiling division of additional minutes
            int additionalMinutes = effectiveMinutes - 60;
            // Ceiling division: (additionalMinutes + 59) / 60
            int additionalHours   = (additionalMinutes + 59) / 60;

            // BigDecimal multiplication for accurate monetary arithmetic
            BigDecimal additionalFee = tariff.getAdditionalHourFee()
                    .multiply(BigDecimal.valueOf(additionalHours));
            fee = fee.add(additionalFee);
        }

        return fee;
    }
}

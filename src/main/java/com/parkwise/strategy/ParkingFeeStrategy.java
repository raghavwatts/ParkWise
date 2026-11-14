package com.parkwise.strategy;

import com.parkwise.model.Tariff;

import java.math.BigDecimal;

/**
 * Strategy interface for parking fee calculation.
 *
 * <p>Design Pattern: <b>Strategy</b> — encapsulates the fee-calculation
 * algorithm so different tariff structures can be swapped without
 * changing the service layer.</p>
 */
public interface ParkingFeeStrategy {

    /**
     * Calculates the parking fee for the given duration using the provided tariff.
     *
     * @param durationMinutes actual parking duration in minutes (must be &gt;= 0)
     * @param tariff          the active tariff for the vehicle's type
     * @return the fee as a non-negative BigDecimal
     */
    BigDecimal calculate(int durationMinutes, Tariff tariff);
}

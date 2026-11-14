package com.parkwise.strategy;

import com.parkwise.model.Tariff;
import com.parkwise.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StandardFeeStrategy (Module 7 fee rules).
 */
class FeeCalculationTest {

    private StandardFeeStrategy strategy;
    private Tariff fourWheelerTariff;
    private Tariff twoWheelerTariff;

    @BeforeEach
    void setUp() {
        strategy = new StandardFeeStrategy();
        fourWheelerTariff = new Tariff(1, VehicleType.FOUR_WHEELER,
                new BigDecimal("40.00"), new BigDecimal("20.00"), true);
        twoWheelerTariff  = new Tariff(2, VehicleType.TWO_WHEELER,
                new BigDecimal("20.00"), new BigDecimal("10.00"), true);
    }

    @Test
    @DisplayName("Zero minutes → first-hour charge only")
    void zeroMinutes_returnsFirstHourFee() {
        BigDecimal fee = strategy.calculate(0, fourWheelerTariff);
        assertEquals(new BigDecimal("40.00"), fee);
    }

    @Test
    @DisplayName("Exactly 60 minutes → first-hour charge only")
    void sixtyMinutes_returnsFirstHourFee() {
        BigDecimal fee = strategy.calculate(60, fourWheelerTariff);
        assertEquals(new BigDecimal("40.00"), fee);
    }

    @Test
    @DisplayName("61 minutes → first + 1 additional hour")
    void sixtyOneMinutes_returnsFirstPlusOneAdditional() {
        BigDecimal fee = strategy.calculate(61, fourWheelerTariff);
        // ₹40 + ₹20 = ₹60
        assertEquals(new BigDecimal("60.00"), fee);
    }

    @Test
    @DisplayName("120 minutes → first + 1 additional hour")
    void oneHundredTwentyMinutes_returnsFirstPlusOne() {
        BigDecimal fee = strategy.calculate(120, fourWheelerTariff);
        assertEquals(new BigDecimal("60.00"), fee);
    }

    @Test
    @DisplayName("121 minutes → first + 2 additional hours")
    void oneHundredTwentyOneMinutes_returnsFirstPlusTwo() {
        BigDecimal fee = strategy.calculate(121, fourWheelerTariff);
        // ₹40 + 2×₹20 = ₹80
        assertEquals(new BigDecimal("80.00"), fee);
    }

    @Test
    @DisplayName("Two-wheeler: 45 minutes → ₹20")
    void twoWheeler_fortyFiveMinutes() {
        BigDecimal fee = strategy.calculate(45, twoWheelerTariff);
        assertEquals(new BigDecimal("20.00"), fee);
    }

    @Test
    @DisplayName("Two-wheeler: 90 minutes → ₹30")
    void twoWheeler_ninetyMinutes() {
        BigDecimal fee = strategy.calculate(90, twoWheelerTariff);
        // ₹20 + ₹10 = ₹30
        assertEquals(new BigDecimal("30.00"), fee);
    }

    @Test
    @DisplayName("Negative duration throws IllegalArgumentException")
    void negativeDuration_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculate(-1, fourWheelerTariff));
    }
}

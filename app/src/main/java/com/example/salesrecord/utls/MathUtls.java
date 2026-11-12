package com.example.salesrecord.utls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for general mathematical and financial calculations.
 */
public final class MathUtls {

    // Constante compartida para evitar instanciar el divisor 100 repetidamente
    private static final BigDecimal CIEN = new BigDecimal("100");

    // Escala por defecto recomendada para cálculos comerciales/monedas
    private static final int ESCALA_MONEDA = 2;

    // Constructor privado para evitar que la clase se instancie
    private MathUtls() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Adds a percentage to a base value.
     */
    public static BigDecimal addPercentage(BigDecimal baseValue, BigDecimal percentage) {
        if (baseValue == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        // Operación: baseValue * (percentage / 100)
        BigDecimal percentageValue = baseValue.multiply(percentage.divide(CIEN, ESCALA_MONEDA, RoundingMode.HALF_UP));

        // Operación: baseValue + percentageValue
        return baseValue.add(percentageValue).setScale(ESCALA_MONEDA, RoundingMode.HALF_UP);
    }

    /**
     * Subtracts a percentage from a base value (useful for discounts).
     */
    public static BigDecimal subtractPercentage(BigDecimal baseValue, BigDecimal percentage) {
        if (baseValue == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        // Operación: baseValue * (percentage / 100)
        BigDecimal percentageValue = baseValue.multiply(percentage.divide(CIEN, ESCALA_MONEDA, RoundingMode.HALF_UP));

        // Operación: baseValue - percentageValue
        return baseValue.subtract(percentageValue).setScale(ESCALA_MONEDA, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the exact value of a percentage from a total.
     * e.g., 10% of 25 returns 2.50
     */
    public static BigDecimal getPercentageValue(BigDecimal totalValue, BigDecimal percentage) {
        if (totalValue == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        // Operación: totalValue * (percentage / 100)
        return totalValue.multiply(percentage.divide(CIEN, ESCALA_MONEDA, RoundingMode.HALF_UP))
                .setScale(ESCALA_MONEDA, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a BigDecimal value to a specific number of decimal places.
     * Prevents common floating-point precision issues.
     */
    public static BigDecimal round(BigDecimal value, int decimalPlaces) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Decimal places cannot be negative");
        }
        return value.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    // Factor de gravedad: controla qué tan rápido decae el ranking con el tiempo.
    // Valores recomendados: entre 1.2 (suave) y 1.8 (estricto).
    private static final double GRAVITY = 0.8;

    public static String setRankingText(float totalSales, long createdAtLong) {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - createdAtLong;

        if (elapsedMillis < 0) {
            elapsedMillis = 0;
        }

        double daysAlive = (double) elapsedMillis / 86400000.0;
        double timePenalty = daysAlive + 2.0;          // GRAVITY = 1.0 → no hace falta Math.pow

        double rankingScore =  (totalSales / timePenalty) * 1000;

        return  String.format(Locale.US, "%.3f", rankingScore);
    }

}
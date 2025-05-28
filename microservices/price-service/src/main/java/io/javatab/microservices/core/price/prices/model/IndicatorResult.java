package io.javatab.microservices.core.price.prices.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndicatorResult {
    private String ticker;
    private double atr;
    private double rsi;
    private double bbUpper;
    private double bbLower;
    private final double maxDrawdown;
    private final long recoveryTimeBars;
}
package io.javatab.microservices.core.app.prices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceDto(
        String ticker,
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}
package io.javatab.microservices.core.app.prices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceDto(
        String ticker,
        LocalDate date,
        BigDecimal close,
        BigDecimal volume
) {
}
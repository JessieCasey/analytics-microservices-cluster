package io.javatab.microservices.core.app.prices.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
public class SMAResult {
    private String ticker;
    private LocalDate date;
    private BigDecimal sma;
}
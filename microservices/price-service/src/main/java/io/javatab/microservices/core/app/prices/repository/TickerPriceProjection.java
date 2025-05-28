package io.javatab.microservices.core.app.prices.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TickerPriceProjection {
  String getTicker();
  LocalDate getDate();
  BigDecimal getClose();
}

// src/main/java/edu/analytics/prices/service/PriceService.java
package io.javatab.microservices.core.price.prices.service;


import io.javatab.microservices.core.price.prices.dto.PriceDto;
import io.javatab.microservices.core.price.prices.model.Price;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface PriceService {
    /** For API endpoints returning DTOs */
    List<PriceDto> getPricesByTicker(String ticker);

    /** For heavy-compute services needing raw entities */
    List<Price> getEntitiesByTickerAndDateRange(String ticker,
                                                LocalDate start,
                                                LocalDate end);

    /** To iterate through all tickers in the collection */
    List<String> getAllTickers();

    Page<PriceDto> getLatestPrices(int page, int size);
}

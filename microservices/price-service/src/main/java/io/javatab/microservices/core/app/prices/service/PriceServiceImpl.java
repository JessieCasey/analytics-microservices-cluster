// src/main/java/edu/analytics/prices/service/impl/PriceServiceImpl.java
package io.javatab.microservices.core.app.prices.service;


import io.javatab.microservices.core.app.prices.dto.PriceDto;
import io.javatab.microservices.core.app.prices.mapper.PriceMapper;
import io.javatab.microservices.core.app.prices.model.Price;
import io.javatab.microservices.core.app.prices.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final PriceRepository repo;
    private final PriceMapper mapper;
    private final MongoTemplate mongo;

    @Override
    public List<PriceDto> getPricesByTicker(String ticker) {
        return repo.findByTickerOrderByDateDesc(ticker).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Price> getEntitiesByTickerAndDateRange(String ticker,
                                                       LocalDate start,
                                                       LocalDate end) {
        return repo.findByTickerAndDateBetweenOrderByDateAsc(ticker, start, end);
    }

    @Override
    public List<String> getAllTickers() {
        return mongo.query(Price.class)
                .distinct("ticker")
                .as(String.class)
                .all();
    }

    public Page<PriceDto> getLatestPrices(int page, int size) {
        long total = 0;
        int skip  = page * size;
        List<Price> hits = repo.findLatestPrices(skip, size);
        List<PriceDto> dtos = hits.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, PageRequest.of(page, size), total);
    }

}

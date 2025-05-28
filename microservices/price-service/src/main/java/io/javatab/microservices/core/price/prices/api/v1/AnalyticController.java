package io.javatab.microservices.core.price.prices.api.v1;

import io.javatab.microservices.core.price.prices.dto.PriceDto;
import io.javatab.microservices.core.price.prices.model.IndicatorResult;
import io.javatab.microservices.core.price.prices.service.PriceService;
import io.javatab.microservices.core.price.prices.service.Ta4jHeavyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/v1/prices")
public class AnalyticController {

    private final Ta4jHeavyService ta4jHeavyService;
    private final PriceService priceService;

    @GetMapping("/indicators")
    public ResponseEntity<List<IndicatorResult>> run(
            @RequestParam List<String> tickers,
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDate from = LocalDate.parse(start);
        LocalDate to = LocalDate.parse(end);
        Map<String, IndicatorResult> map = ta4jHeavyService.computeForTickers(tickers, from, to);
        if (map.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new ArrayList<>(map.values()));
    }

    @GetMapping("/tickers")
    public ResponseEntity<Page<PriceDto>> getTickers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PriceDto> result = priceService.getLatestPrices(page, size);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}

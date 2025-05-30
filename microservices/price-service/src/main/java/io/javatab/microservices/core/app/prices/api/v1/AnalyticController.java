package io.javatab.microservices.core.app.prices.api.v1;

import io.javatab.microservices.core.app.prices.dto.PriceDto;
import io.javatab.microservices.core.app.prices.model.IndicatorResult;
import io.javatab.microservices.core.app.prices.model.Price;
import io.javatab.microservices.core.app.prices.model.SMAResult;
import io.javatab.microservices.core.app.prices.repository.PriceRepository;
import io.javatab.microservices.core.app.prices.service.PriceService;
import io.javatab.microservices.core.app.prices.service.Ta4jHeavyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/v1/prices")
public class AnalyticController {

    private final Ta4jHeavyService ta4jHeavyService;
    private final PriceService priceService;
    private final MongoTemplate mongoTemplate;

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

    @GetMapping("/indicators/historical/{ticker}")
    public ResponseEntity<List<SMAResult>> getHistorical(
            @PathVariable String ticker,
            @RequestParam int window
    ) {
        var results = ta4jHeavyService.computeSimpleMovingAverage(ticker, window);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }


    @PostMapping("/load-data")
    public ResponseEntity<String> loadDataToDb(@RequestBody List<Price> incomingPrices) {
        int upsertedCount = 0;

        for (Price incoming : incomingPrices) {
            // Build the Mongo “query” to find by ticker+date:
            Query query = Query.query(
                    Criteria.where("ticker").is(incoming.getTicker())
                            .and("date").is(incoming.getDate())
            );

            // Build an Update object to set all fields you care about:
            Update update = new Update()
                    .set("open", incoming.getOpen())
                    .set("high", incoming.getHigh())
                    .set("low", incoming.getLow())
                    .set("close", incoming.getClose())
                    .set("volume", incoming.getVolume())
                    // If you want the “ticker” and “date” fields in a brand‐new insert, do:
                    .setOnInsert("ticker", incoming.getTicker())
                    .setOnInsert("date", incoming.getDate());

            // This upsert() either updates the existing doc or inserts a new one:
            mongoTemplate.upsert(query, update, Price.class);
            upsertedCount++;
        }

        return ResponseEntity.ok("Upserted " + upsertedCount + " records.");
    }
}

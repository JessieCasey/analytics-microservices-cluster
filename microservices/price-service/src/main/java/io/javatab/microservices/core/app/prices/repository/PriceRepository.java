package io.javatab.microservices.core.app.prices.repository;


import io.javatab.microservices.core.app.prices.model.Price;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PriceRepository extends MongoRepository<Price, String> {
    List<Price> findPriceByTicker(String ticker);

    List<Price> findByTickerOrderByDateDesc(String ticker);

    List<Price> findByTickerAndDateBetweenOrderByDateAsc(String ticker, LocalDate start, LocalDate end);

    @Aggregation(pipeline = {
            "{ '$sort': { 'date': -1 } }",
            "{ '$group': { " +
                    "_id: '$ticker', " +
                    "ticker: { '$first': '$ticker' }, " +
                    "date:   { '$first': '$date'   }, " +
                    "close:  { '$first': '$close'  }," +
                    "open:  { '$first': '$open'  }," +
                    "high:  { '$first': '$high'  }," +
                    "low:  { '$first': '$low' }," +
                    "volume:  { '$first': '$volume'  }" +
                    "} }",
            "{ '$skip': ?0 }",
            "{ '$limit': ?1 }"
    })
    List<Price> findLatestPrices(int skip, int limit);

    List<Price> findAllByTickerOrderByDate(String ticker);

}

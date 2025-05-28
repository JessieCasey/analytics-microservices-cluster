package io.javatab.microservices.core.app.prices.api.v1;

import io.javatab.microservices.core.app.common.BaseController;
import io.javatab.microservices.core.app.prices.dto.PriceDto;
import io.javatab.microservices.core.app.prices.mapper.PriceMapper;
import io.javatab.microservices.core.app.prices.model.Price;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/v1/prices")
public class PricesController extends BaseController<Price, PriceDto> {

    public PricesController(MongoTemplate mongo, PriceMapper priceMapper) {
        super(mongo, Price.class, priceMapper);
    }
}

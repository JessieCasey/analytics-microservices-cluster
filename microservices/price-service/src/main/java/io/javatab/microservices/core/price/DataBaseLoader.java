package io.javatab.microservices.core.price;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javatab.microservices.core.price.prices.model.Price;
import io.javatab.microservices.core.price.prices.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DataBaseLoader {

    @Bean
    CommandLineRunner initMongo(PriceRepository pricesRepo, ObjectMapper mapper) {
        return args -> {
//            log.info("Loading prices, less data ...");
//            pricesRepo.deleteAll();
//
//            InputStream is = new ClassPathResource("data/initial-data.json").getInputStream();
//            List<Price> prices = Arrays.asList(mapper.readValue(is, Price[].class));
//
//            pricesRepo.saveAll(prices);
//
//            System.out.println("✔️ MongoDB initialized with seed data");
        };
    }

}

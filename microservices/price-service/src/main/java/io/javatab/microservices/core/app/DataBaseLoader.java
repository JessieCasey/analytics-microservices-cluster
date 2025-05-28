package io.javatab.microservices.core.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javatab.microservices.core.app.prices.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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

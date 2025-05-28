package io.javatab.microservices.core.price;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication
public class PriceServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(PriceServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(PriceServiceApplication.class, args);

		String mongoDbUri = ctx.getEnvironment().getProperty("spring.data.mongodb.uri");
        logger.info("Connected to MongoDb ===> {}", mongoDbUri);
	}

}

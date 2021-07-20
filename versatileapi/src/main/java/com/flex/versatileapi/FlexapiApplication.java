package com.flex.versatileapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;


@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class FlexapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlexapiApplication.class, args);
	}

}

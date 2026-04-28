package it.adesso.awesomepizza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AwesomePizzaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwesomePizzaApplication.class, args);
	}

}

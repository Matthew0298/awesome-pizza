package it.adesso.awesomepizza.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI awesomePizzaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Awesome Pizza API")
                        .description("Enterprise-ready API to manage pizza orders and workflow statuses")
                        .version("v1")
                        .contact(new Contact()
                                .name("Awesome Pizza Team")
                                .email("support@awesomepizza.local")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local environment")
                ));
    }
}

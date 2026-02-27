package uz.myrafeeq.api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPI myRafeeqOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("MyRafeeq API")
                .description(
                    """
                    Islamic companion Telegram Mini App backend. Provides authentication \
                    via Telegram init data, prayer time calculations, prayer tracking, \
                    and user preferences management.""")
                .version("0.0.1")
                .contact(new Contact().name("MyRafeeq").url("https://github.com/myrafeeq")))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local development"),
                new Server().url("https://api.myrafeeq.uz").description("Production")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtained from POST /api/v1/auth/token"))
                .addSecuritySchemes(
                    "adminApiKey",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Admin-Api-Key")
                        .description("Admin API key for management endpoints")));
  }
}

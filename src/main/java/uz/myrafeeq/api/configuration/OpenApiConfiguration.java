package uz.myrafeeq.api.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                .contact(new Contact().name("MyRafeeq").url("https://github.com/myrafeeq")));
  }
}

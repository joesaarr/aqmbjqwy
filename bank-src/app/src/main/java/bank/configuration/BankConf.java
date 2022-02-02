package bank.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableOpenApi
public class BankConf {

    public static final Set<Currency> ALLOWED_CURRENCIES = Stream.of("EUR", "SEK", "GBP", "USD")
            .map(Currency::getInstance).collect(Collectors.toSet());

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

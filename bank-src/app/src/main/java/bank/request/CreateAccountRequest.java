package bank.request;

import bank.enums.Country;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Currency;
import java.util.Set;

/**
 * Request object for creating a new account
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CreateAccountRequest {
    @NotEmpty(message = "customer ID must not be empty")
    String customerId;
    @NotNull(message = "country must not be null")
    Country country;
    @NotEmpty(message = "must have at least one currency")
    Set<Currency> currencies;
}

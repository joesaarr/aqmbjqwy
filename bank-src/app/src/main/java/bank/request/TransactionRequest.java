package bank.request;

import bank.enums.TransactionDirection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Request object for creating a new transaction
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TransactionRequest {
    @NotNull(message = "account ID must not be null")
    Long accountId;
    @NotNull(message = "amount must not be null")
    @Positive(message = "amount must be greater than 0")
    BigDecimal amount;
    @NotNull(message = "currency must not be null")
    Currency currency;
    @NotNull(message = "direction must not be null")
    TransactionDirection direction;
    @NotEmpty(message = "description must not be empty")
    String description;
}

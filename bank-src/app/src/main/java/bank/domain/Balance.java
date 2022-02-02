package bank.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Currency;

@Data
@Accessors(chain = true)
public class Balance {
    private Long id;
    private Currency currency;
    private BigDecimal amount;
}

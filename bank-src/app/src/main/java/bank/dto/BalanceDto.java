package bank.dto;

import bank.domain.Balance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Currency;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceDto {

    Currency currency;
    BigDecimal amount;

    public static BalanceDto from(Balance balance) {
        return new BalanceDto(
                balance.getCurrency(),
                balance.getAmount()
        );
    }
}

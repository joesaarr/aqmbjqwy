package bank.dto;

import bank.domain.Account;
import bank.enums.Country;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountDto {

    Long id;
    String customerId;
    Country country;
    List<BalanceDto> balances;

    public static AccountDto from(Account account) {
        return new AccountDto(
                account.getId(),
                account.getCustomerId(),
                account.getCountry(),
                account.getBalances().stream()
                        .map(BalanceDto::from)
                        .collect(Collectors.toList())
        );
    }
}

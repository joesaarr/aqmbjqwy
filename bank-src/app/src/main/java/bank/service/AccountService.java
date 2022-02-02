package bank.service;

import bank.configuration.BankConf;
import bank.configuration.RabbitMqConf;
import bank.domain.Account;
import bank.domain.Balance;
import bank.dto.AccountDto;
import bank.exception.AccountNotFoundException;
import bank.exception.InvalidCurrencyException;
import bank.data.AccountMapper;
import bank.data.BalanceMapper;
import bank.request.CreateAccountRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Account service
 */
@Service
public class AccountService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    BalanceMapper balanceMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * Create new account
     * @param request account creation request
     * @return created account dto
     */
    public AccountDto createAccount(CreateAccountRequest request) {
        var account = new Account()
                .setCustomerId(request.getCustomerId())
                .setCountry(request.getCountry());
        accountMapper.insertAccount(account);

        if (!BankConf.ALLOWED_CURRENCIES.containsAll(request.getCurrencies())) {
            throw new InvalidCurrencyException();
        }

        request.getCurrencies().forEach(currency -> {
            var balance = new Balance()
                    .setAmount(BigDecimal.ZERO)
                    .setCurrency(currency);
            balanceMapper.insertBalance(balance, account);
            account.getBalances().add(balance);
        });

        var accountDto = AccountDto.from(account);

        rabbitTemplate.convertAndSend(RabbitMqConf.EXCHANGE_NAME, "create-account", accountDto);

        return AccountDto.from(account);
    }

    /**
     * Find account by account id
     * @param id account id
     * @return account dto
     */
    public AccountDto find(Long id) {
        return AccountDto.from(
                accountMapper.selectAccount(id).orElseThrow(() -> new AccountNotFoundException(id))
        );
    }
}

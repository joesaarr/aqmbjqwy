package bank.service;

import bank.configuration.RabbitMqConf;
import bank.domain.Transaction;
import bank.dto.TransactionDto;
import bank.enums.TransactionDirection;
import bank.exception.AccountNotFoundException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidCurrencyException;
import bank.data.AccountMapper;
import bank.data.BalanceMapper;
import bank.data.TransactionMapper;
import bank.request.TransactionRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public TransactionDto createTransaction(TransactionRequest request) {
        var account = accountMapper.selectAccount(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        var balance = account.getBalances().stream()
                .filter(b -> b.getCurrency().equals(request.getCurrency()))
                .findAny().orElseThrow(InvalidCurrencyException::new);

        balance.setAmount(calculateNewBalance(
                balance.getAmount(), request.getDirection(), request.getAmount()
        ));

        var transaction = new Transaction()
                .setAccountId(account.getId())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .setDirection(request.getDirection())
                .setDescription(request.getDescription())
                .setBalanceAfter(balance.getAmount());

        balanceMapper.updateBalance(balance);
        transactionMapper.insertTransaction(transaction);

        var transactionDto = TransactionDto.from(transaction);

        rabbitTemplate.convertAndSend(RabbitMqConf.EXCHANGE_NAME, "create-transaction", transactionDto);

        return transactionDto;
    }

    public List<TransactionDto> findAllByAccountId(Long accountId) {
        accountMapper.selectAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return transactionMapper.selectByAccountId(accountId).stream()
                .map(TransactionDto::from)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateNewBalance(BigDecimal balanceAmount,
                                           TransactionDirection direction,
                                           BigDecimal transactionAmount) {
        BigDecimal newBalance = switch (direction) {
            case IN -> balanceAmount.add(transactionAmount);
            case OUT -> balanceAmount.subtract(transactionAmount);
        };
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException();
        }
        return newBalance;
    }
}

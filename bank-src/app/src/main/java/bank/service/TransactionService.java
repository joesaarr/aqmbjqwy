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

/**
 * Transaction service
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Create new transaction
     * @param request transaction request
     * @return transaction dto
     */
    public TransactionDto createTransaction(TransactionRequest request) {
        var account = accountMapper.selectAccount(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        var balance = account.getBalances().stream()
                .filter(b -> b.getCurrency().equals(request.getCurrency()))
                .findAny().orElseThrow(InvalidCurrencyException::new);

        var balanceChange = calculateBalanceChange(request.getDirection(), request.getAmount());

        var newBalance = balance.getAmount().add(balanceChange);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException();
        }

        balanceMapper.updateBalance(balance, balanceChange);

        var transaction = new Transaction()
                .setAccountId(account.getId())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .setDirection(request.getDirection())
                .setDescription(request.getDescription())
                .setBalanceAfter(newBalance);

        transactionMapper.insertTransaction(transaction);

        var transactionDto = TransactionDto.from(transaction);

        rabbitTemplate.convertAndSend(RabbitMqConf.EXCHANGE_NAME, "create-transaction", transactionDto);

        return transactionDto;
    }

    /**
     * Find all transactions of account
     * @param accountId account id
     * @return list of transaction dtos
     */
    public List<TransactionDto> findAllByAccountId(Long accountId) {
        accountMapper.selectAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return transactionMapper.selectByAccountId(accountId).stream()
                .map(TransactionDto::from)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateBalanceChange(TransactionDirection direction, BigDecimal transactionAmount) {
        return switch (direction) {
            case IN -> transactionAmount;
            case OUT -> transactionAmount.negate();
        };
    }
}

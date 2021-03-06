package bank.data;

import bank.domain.Account;
import bank.domain.Balance;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface BalanceMapper {

    void insertBalance(Balance balance, Account account);

    void updateBalance(Balance balance, BigDecimal balanceChange);

}

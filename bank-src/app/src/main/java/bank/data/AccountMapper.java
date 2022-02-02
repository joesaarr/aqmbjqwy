package bank.data;

import bank.domain.Account;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface AccountMapper {

    Optional<Account> selectAccount(Long id);

    void insertAccount(Account account);

}

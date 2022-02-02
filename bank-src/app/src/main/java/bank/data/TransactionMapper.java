package bank.data;

import bank.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TransactionMapper {

    void insertTransaction(Transaction transaction);

    List<Transaction> selectByAccountId(Long accountId);

}

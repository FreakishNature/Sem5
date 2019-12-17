package com.database;

import com.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findFirstByEmail(String email);
    Optional<Account> findFirstByUsername(String username);
    List<Account> findAllByRole(String role);
//    List<Account> findTopByInvestedSum(double investedSum);
//    List<Account> findTopNByInvestedSum(int top);
}

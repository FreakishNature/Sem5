package com.controllers;

import com.database.AccountRepository;
import com.database.ProjectMembersRepository;
import com.database.ProjectRepository;
import com.entities.Account;
import com.model.User;
import com.model.UserData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class InvestorsController {

    private static Logger log = Logger.getLogger(ProjectsController.class);

    private final
    ProjectRepository projectRepository;

    private final
    AccountRepository accountRepository;

    private final
    ProjectMembersRepository projectMembersRepository;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    public InvestorsController(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }


    @GetMapping("/users")
    ResponseEntity getInvestors(
            @RequestParam(required = false) Optional<Integer> top,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") int fetchingAmount){
        if(top.isPresent()){
            List<Account> accounts = accountRepository.findTopByInvestedSum(1000);
            return getPageForListOfAccounts(page, fetchingAmount, accounts);
        }
        List<Account> accounts = accountRepository.findAll();
        return getPageForListOfAccounts(page, fetchingAmount, accounts);
    }

    private ResponseEntity getPageForListOfAccounts(Integer page,int fetchingAmount, List<Account> accounts) {
        List<UserData> userData = accounts.stream()
                .map(UserData::new)
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                userData.subList(page * fetchingAmount, userData.size() < (page + 1) * fetchingAmount ?
                        userData.size() :
                        (page + 1) * fetchingAmount
                ),
                HttpStatus.OK
        );
    }


}

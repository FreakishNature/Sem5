package com.controllers;

import com.database.AccountRepository;
import com.database.ProjectMembersRepository;
import com.database.ProjectRepository;
import com.entities.Account;
import com.model.UserData;
import com.response.ErrorResponse;
import com.security.Authorities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UsersController {

    private static Logger log = Logger.getLogger(ProjectsController.class);

    private final
    ProjectRepository projectRepository;

    private final
    AccountRepository accountRepository;

    private final
    ProjectMembersRepository projectMembersRepository;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    public UsersController(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }

    // TO DO: to do user response without role
    @GetMapping("{username}")
    ResponseEntity getUser(@PathVariable String username){
        Optional<Account> accountOptional = accountRepository.findFirstByUsername(username);

        return accountOptional.<ResponseEntity>map(
                account ->
                        new ResponseEntity<>(
                                new UserData(account),
                                HttpStatus.OK
                        ))
                .orElseGet(() ->
                        new ResponseEntity<>(
                                ErrorResponse.errorResponseNoSuchUsername(username),
                                HttpStatus.NOT_FOUND
                        ));

    }

    @GetMapping
    ResponseEntity getUsers(
            @RequestParam(required = false) Optional<Integer> top,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false) Optional<Integer> fetchingAmount,
            @RequestParam(required = false) Optional<String> role){
        List<Account> accounts;

        if(role.isPresent()){
            if(!Authorities.getListOfAuthorities().contains(role.get())){
                return responseWithLogs(new ResponseEntity<>(new ErrorResponse("No such role " + role),HttpStatus.BAD_REQUEST));
            }

            accounts = accountRepository.findAllByRole(role.get());

            if(top.isPresent()){
                if(top.get() <= 0){
                    return responseWithLogs(new ResponseEntity<>(new ErrorResponse("Top should be greater than 0"),HttpStatus.BAD_REQUEST));
                }
                if(role.get().equals(Authorities.INVESTOR)){
                    accounts.sort((act1, act2) -> (int) (act1.getInvestedSum() - act2.getInvestedSum()));
                    Collections.reverse(accounts);
                    accounts = accounts.stream()
                            .filter(Account::isPublicInvestor)
                            .collect(Collectors.toList());
                }
                accounts = accounts.subList(0,top.get() > accounts.size() ? accounts.size() : top.get());
            }
        } else {
            accounts = accountRepository.findAll();
        }

        return getPageForListOfAccounts(page,
                fetchingAmount.isPresent() ? fetchingAmount.get() : accounts.size(),
                accounts
        );
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

    private ResponseEntity responseWithLogs(ResponseEntity<ErrorResponse> responseEntity){
        log.info("Has been returned response : " + responseEntity);
        return responseEntity;
    }
}

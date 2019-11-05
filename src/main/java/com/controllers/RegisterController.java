package com.controllers;

import com.database.AccountRepository;
import com.model.Account;
import com.response.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    AccountRepository accountRepository;

    @PostMapping
    ResponseEntity<ErrorResponse> registerAccount(@RequestBody Account account){
        if(accountRepository.findFirstByUsername(account.getUsername()) != null ){
            return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingAccount("username"),HttpStatus.BAD_REQUEST);
        }
        if(accountRepository.findFirstByEmail(account.getEmail()) != null){
            return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingAccount("email"),HttpStatus.BAD_REQUEST);
        }

        accountRepository.save(account);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
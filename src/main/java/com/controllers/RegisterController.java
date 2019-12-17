package com.controllers;

import com.database.AccountRepository;
import com.entities.Account;
import com.model.UpdateAccountRequest;
import com.response.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    AccountRepository accountRepository;

    @PostMapping
    ResponseEntity<ErrorResponse> registerAccount(@RequestBody Account account){
        if(accountRepository.findFirstByUsername(account.getUsername()).isPresent()){
            return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingField("username"),HttpStatus.BAD_REQUEST);
        }
        if(accountRepository.findFirstByEmail(account.getEmail()).isPresent()){
            return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingField("email"),HttpStatus.BAD_REQUEST);
        }

        accountRepository.save(account);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/update")
    ResponseEntity<ErrorResponse> updateUserInfo(Authentication auth,
                                  @RequestBody UpdateAccountRequest updateAccountRequest){
        if(auth == null){
            return new ResponseEntity<>(new ErrorResponse("Unauthorized."),HttpStatus.UNAUTHORIZED);
        }

        Optional<Account> accountOptional = accountRepository.findFirstByUsername(auth.getPrincipal().toString());
        Account account = accountOptional.get();

        if(updateAccountRequest.getPassword() != null && updateAccountRequest.getOldPassword() != null){
            if(!account.getPassword().equals(updateAccountRequest.getOldPassword())){
                return new ResponseEntity<>(new ErrorResponse("Invalid password"),HttpStatus.BAD_REQUEST);
            } else {
                account.setPassword(updateAccountRequest.getPassword());
            }

        }
        if(updateAccountRequest.getMail() != null){
            if(accountRepository.findFirstByEmail(updateAccountRequest.getMail()).isPresent()){
                return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingField("mail"),HttpStatus.BAD_REQUEST);
            } else {
                account.setEmail(updateAccountRequest.getMail());
            }
        }

        if(updateAccountRequest.getAbout() != null){
            account.setAbout(updateAccountRequest.getAbout());
        }

        if(updateAccountRequest.getImageUri() != null){
            account.setUserImage(updateAccountRequest.getImageUri());


        }
        accountRepository.save(account);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}

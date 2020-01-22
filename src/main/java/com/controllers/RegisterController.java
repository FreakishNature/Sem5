package com.controllers;

import com.auth0.jwt.JWT;
import com.database.AccountRepository;
import com.entities.Account;
import com.model.UpdateAccountRequest;
import com.response.ErrorResponse;
import com.security.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@RestController
@RequestMapping("/register")
public class RegisterController {

    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @PostMapping
    ResponseEntity<ErrorResponse> registerAccount(@RequestBody Account account){
        if(accountRepository.findFirstByUsername(account.getUsername()).isPresent()){
            return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingField("username"),HttpStatus.BAD_REQUEST);
        }
        if(accountRepository.findFirstByEmail(account.getEmail()).isPresent()){
            return new ResponseEntity<>(ErrorResponse.errorResponseWithExistingField("email"),HttpStatus.BAD_REQUEST);
        }

        accountRepository.save(account);

        Date expiresAt = new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME);
        System.out.println("test");
        String token = JWT.create()
                .withSubject(account.getUsername())
                .withExpiresAt(expiresAt)
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));


        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("crowdfundingfaf@gmail.com");
        mailSender.setPassword("Nickita98");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("Registration confirmation");

        mailMessage.setText("Confirm you account by redirecting to this url : http://localhost:8081/register?token=" + token);

        mailSender.send(mailMessage);


        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    ResponseEntity registerConfirmation(@RequestParam String token){
        String userName = JWT.require(HMAC512(JwtProperties.SECRET.getBytes()))
                .acceptExpiresAt(JwtProperties.EXPIRATION_TIME)
                .build()
                .verify(token)
                .getSubject();
        Optional<Account> account = accountRepository.findFirstByUsername(userName);

        if(account.isPresent()){
            account.get().setEnabled(true);
            return new ResponseEntity(HttpStatus.CREATED);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

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

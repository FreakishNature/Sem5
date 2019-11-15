package com.security.auth;

import com.auth0.jwt.JWT;
import com.database.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.entities.Account;
import com.model.User;
import com.security.UserPrincipal;
import com.response.ErrorResponse;
import com.security.JwtProperties;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    Logger log = Logger.getLogger(JwtAuthenticationFilter.class);
    AccountRepository accountRepository;

    private AuthenticationManager authenticationManager;
    private ObjectMapper mapper = new ObjectMapper();
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.authenticationManager = authenticationManager;
    }

    private void errorResponse(HttpServletResponse response,String errorMsg){
        try {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setHeader("Content-Type","application/json");
            response.getOutputStream().write(mapper.writeValueAsBytes(new ErrorResponse(errorMsg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void errorResponseMissingUserFields(HttpServletResponse response,User user){
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setHeader("Content-Type","application/json");
        try {
            response.getOutputStream()
                    .write(mapper.writeValueAsBytes(
                            ErrorResponse.errorResponseWithMissingField(
                                    user.getUsername() == null ? "username" : "password")
                            )
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        User user = null;
        Optional<Account> account;

        try {
            user = mapper.readValue(request.getInputStream(),User.class);
            account = accountRepository.findFirstByUsername(user.getUsername());
        } catch (IOException e) {
            errorResponse(response,"Invalid request body.");
            return null;
        }

        if(user.getUsername() == null || user.getPassword() == null){
                errorResponseMissingUserFields(response,user);
                return null;
        }

        log.info("Trying to get token for user : " + user.getUsername());
        // Account not found
        if(!account.isPresent()) {
            errorResponse(response,"Invalid username or password.");
            return null;
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                account.get().getUsername(),
                account.get().getPassword()
        );

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
        String token = JWT.create()
                .withSubject(principal.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));

        log.info("Token has been generated successfully");
        log.debug("Generated token : " + token);
        // Add token in response
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + " " +  token);
    }


}

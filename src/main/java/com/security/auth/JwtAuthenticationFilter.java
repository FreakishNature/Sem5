package com.security.auth;

import com.auth0.jwt.JWT;
import com.database.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Account;
import com.model.User;
import com.model.UserPrincipal;
import com.response.ErrorResponse;
import com.security.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    AccountRepository accountRepository;

    private AuthenticationManager authenticationManager;
    private ObjectMapper mapper = new ObjectMapper();
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.authenticationManager = authenticationManager;
    }

    private void errorResponse(HttpServletResponse response,String errorMsg){
        try {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("Content-Type","application/json");
            response.getOutputStream().write(mapper.writeValueAsBytes(new ErrorResponse(errorMsg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        User user = null;
        Account account = null;
        try {
            user = mapper.readValue(request.getInputStream(),User.class);
            account = accountRepository.findFirstByUsername(user.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Account not found
        if(account == null) {
            errorResponse(response,"Unauthorized. User " + user.getUsername() + " was not found");
            return null;
        }
        // Invalid password
        if(!account.getPassword().equals(user.getPassword())) {
            errorResponse(response,"Unauthorized. Invalid password.");
            return null;
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                account.getUsername(),
                account.getPassword()
        );

        Authentication auth = authenticationManager.authenticate(authenticationToken);

        return auth;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
        String token = JWT.create()
                .withSubject(principal.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .sign(HMAC512(JwtProperties.SECRET.getBytes()));

        // Add token in response
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + " " +  token);
    }


}

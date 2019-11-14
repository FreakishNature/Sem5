package com.security.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.database.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.entities.Account;
import com.security.UserPrincipal;
import com.response.ErrorResponse;
import com.security.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    @Autowired
    private AccountRepository userRepository;
    private ObjectMapper mapper = new ObjectMapper();

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, AccountRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    private void unauthorizedError(HttpServletResponse response){
        try {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("Content-Type","application/json");
            response.getOutputStream().write(mapper.writeValueAsBytes(new ErrorResponse("Unauthorized.")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Read the Authorization header, where the JWT token should be
        String header = request.getHeader(JwtProperties.HEADER_STRING);

        // If header does not contain BEARER or is null delegate to Spring impl and exit
        if (header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
//            unauthorizedError(response);
            chain.doFilter(request, response);
            return;
        }

        // If header is present, try grab user principal from database and perform authorization
        Optional<Authentication> authentication = getUsernamePasswordAuthentication(request,response);
        if(!authentication.isPresent()){
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication.get());

        // Continue filter execution
        chain.doFilter(request, response);
    }

    private Optional<Authentication> getUsernamePasswordAuthentication(HttpServletRequest request,HttpServletResponse response) {
        String token = request.getHeader(JwtProperties.HEADER_STRING)
                .replace(JwtProperties.TOKEN_PREFIX + " ","");

        // parse the token and validate it
        try {
            String userName = JWT.require(HMAC512(JwtProperties.SECRET.getBytes()))
                    .acceptExpiresAt(JwtProperties.EXPIRATION_TIME)
                    .build()
                    .verify(token)
                    .getSubject();

            // Search in the DB if we find the user by token subject (username)
            // If so, then grab user details and create spring auth token using username, pass, authorities/roles
            if (userName != null) {
                Optional<Account> user = userRepository.findFirstByUsername(userName);
                UserPrincipal principal = new UserPrincipal(user.get());
                return Optional.of(new UsernamePasswordAuthenticationToken(userName, null, principal.getAuthorities()));
            }
        }catch (TokenExpiredException | SignatureVerificationException e){
            unauthorizedError(response);
        }

        return Optional.empty();
    }
}

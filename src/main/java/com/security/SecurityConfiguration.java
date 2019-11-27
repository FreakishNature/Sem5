package com.security;

import com.database.AccountRepository;
import com.security.auth.JwtAuthenticationFilter;
import com.security.auth.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private UserPrincipalDetailService userPrincipalDetailsService;
    private AccountRepository accountRepository;

    @Value("${authorities.admin}")
    private String ADMIN;
    @Value("${authorities.projectOwner}")
    private String PROJECT_OWNER;
    @Value("${authorities.moderator}")
    private String MODERATOR;
    @Value("${authorities.investor}")
    private String INVESTOR;
    @Value("${authorities.projectMember}")
    private String PROJECT_MEMBER;



    public SecurityConfiguration(UserPrincipalDetailService userPrincipalDetailsService, AccountRepository accountRepository) {
        this.userPrincipalDetailsService = userPrincipalDetailsService;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        Authorities.ADMIN = ADMIN;
        Authorities.INVESTOR = INVESTOR;
        Authorities.MODERATOR = MODERATOR;
        Authorities.PROJECT_MEMBER = PROJECT_MEMBER;
        Authorities.PROJECT_OWNER = PROJECT_OWNER;

        http
                // remove csrf and state in session because in jwt we do not need them
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // add jwt jsonFilters (1. authentication, 2. authorization)
                .addFilter(new JwtAuthenticationFilter(authenticationManager(),this.accountRepository))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(),  this.accountRepository))
                .authorizeRequests()
                // configure access rules
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/register").permitAll()
                .antMatchers(HttpMethod.GET, "/investors").permitAll()
                .antMatchers(HttpMethod.GET, "/projects").permitAll()
                .antMatchers(HttpMethod.GET, "/projects/*").permitAll()
                .antMatchers(HttpMethod.POST, "/projects")
                .hasAnyRole(Authorities.ADMIN,Authorities.PROJECT_OWNER)
//                .antMatchers(HttpMethod.POST, "/projects/{ownerName}")
//                .hasAnyRole("ADMIN","PROJECT_OWNER")

//                .antMatchers("/api/public/management/*").hasRole("MANAGER")
//                .antMatchers("/api/public/admin/*").hasRole("ADMIN")
                .anyRequest().authenticated().and().cors();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(this.userPrincipalDetailsService);

        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
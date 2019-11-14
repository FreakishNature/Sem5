package com.database;

import com.entities.Account;
import com.security.Authorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCommandLineRunner implements CommandLineRunner {
    private ProjectRepository projectRepository;
    private AccountRepository accountRepository;
    private ProjectMembersRepository projectMembersRepository;

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

    @Autowired
    public DatabaseCommandLineRunner(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Authorities.ADMIN = ADMIN;
        Authorities.INVESTOR = INVESTOR;
        Authorities.MODERATOR = MODERATOR;
        Authorities.PROJECT_MEMBER = PROJECT_MEMBER;
        Authorities.PROJECT_OWNER = PROJECT_OWNER;

        accountRepository.save(new Account(
                "admin",
                "admin",
                "admin@mail.com",
                ADMIN
        ));

        accountRepository.save(new Account(
                "user",
                "user",
                "admin@mail.com",
                PROJECT_MEMBER
        ));
    }
}

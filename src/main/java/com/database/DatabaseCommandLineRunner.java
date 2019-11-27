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

    @Autowired
    public DatabaseCommandLineRunner(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }

    @Override
    public void run(String... args) throws Exception {

    }
}

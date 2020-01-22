package com.database;

import com.entities.Account;
import com.security.Authorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class DatabaseCommandLineRunner implements CommandLineRunner {
    private ProjectRepository projectRepository;
    private AccountRepository accountRepository;
    private ProjectMembersRepository projectMembersRepository;

    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();


    @Autowired
    public DatabaseCommandLineRunner(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }

    @Override
    public void run(String... args) throws Exception {

//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailSender.setHost("smtp.gmail.com");
//        mailSender.setPort(587);
//
//        mailSender.setUsername("crowdfundingfaf@gmail.com");
//        mailSender.setPassword("Nickita98");
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//
//        mailMessage.setTo("crowdfundingfaf@gmail.com");
//        mailMessage.setSubject("Registration confirmation");
//
//        mailMessage.setText("Confirm you account by redirecting to this url : http://localhost:8081/register?token=" );
//
//        mailSender.send(mailMessage);
    }
}

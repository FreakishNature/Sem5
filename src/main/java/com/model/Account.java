package com.model;


import com.security.Authorities;
import com.security.converters.CryptoConverterAes;
import com.security.converters.CryptoConverterShaRsa;
import lombok.Getter;

import javax.persistence.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "Accounts")
@Getter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 512)
    @Convert(converter = CryptoConverterAes.class)
    private String username;

    @Column(length = 512)
    @Convert(converter = CryptoConverterAes.class)
    private String password;

    @Column(length = 512)
    @Convert(converter = CryptoConverterAes.class)
    private String email;

    private String role; // ProjectOwner, ProjectMember ,Investor, Moderator, Admin

    public static List<String> getAuthoritiesForRole(String role) throws IOException {
        switch (role){
            case "ADMIN": return Arrays.asList(Authorities.ADMIN.split(","));
            case "PROJECT_OWNER": return Arrays.asList(Authorities.PROJECT_OWNER.split(","));
            case "PROJECT_MEMBER": return Arrays.asList(Authorities.PROJECT_MEMBER.split(","));
            case "MODERATOR": return Arrays.asList(Authorities.MODERATOR.split(","));
            case "INVESTOR": return Arrays.asList(Authorities.INVESTOR.split(","));
        }
        throw new IOException("There is no such role " + role);
    }
}

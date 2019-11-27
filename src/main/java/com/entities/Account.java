package com.entities;


import com.security.Authorities;
import com.security.converters.CryptoConverterAes;
import com.security.converters.CryptoConverterShaRsa;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "Accounts")
@Getter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512)
//    @Convert(converter = CryptoConverterAes.class)
    private String username;

    @Column(length = 512)
    @Convert(converter = CryptoConverterAes.class)
    private String password;

    @Column(length = 512)
    @Convert(converter = CryptoConverterAes.class)
    private String email;

    @Setter
    private String role; // ProjectOwner, ProjectMember ,Investor, Moderator, Admin

    @Getter
    private double investedSum;

    @Getter
    @Setter
    String about;

    @Getter
    @Setter
    String userImage;

    public void addInvestedSum(double amount){
        investedSum += amount;
    }

    public Account(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

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

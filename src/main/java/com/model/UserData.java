package com.model;

import com.entities.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
public class UserData {
    private String about;
    private double investedSum;
    private String name;
    private String userImage;
    private String role;

    public UserData(Account account) {
        this.about = account.getAbout();
        this.investedSum = account.getInvestedSum();
        this.name = account.getUsername();
        this.userImage = account.getUserImage();
        this.role = account.getRole();
    }
}

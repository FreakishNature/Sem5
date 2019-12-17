package com.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProjectMemberData {
    String name;
    String role;
    double investedSum;
    String membershipScope;
    String userImage;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String status;

    public ProjectMemberData(String name, String role, double investedSum,String membershipScope, String userImage) {
        this.name = name;
        this.role = role;
        this.investedSum = investedSum;
        this.membershipScope = membershipScope;
        this.userImage = userImage;
    }

    public ProjectMemberData(String name, String role, double investedSum,String membershipScope,String userImage, String status) {
        this.name = name;
        this.role = role;
        this.investedSum = investedSum;
        this.membershipScope = membershipScope;
        this.status = status;
        this.userImage = userImage;
    }
}

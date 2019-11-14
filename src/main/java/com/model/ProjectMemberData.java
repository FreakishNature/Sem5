package com.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProjectMemberData {
    String name;
    String role;
    double investedSum;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String status;

    public ProjectMemberData(String name, String role, double investedSum) {
        this.name = name;
        this.role = role;
        this.investedSum = investedSum;
    }
}

package com.entities;

import com.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "ProjectMember")
@NoArgsConstructor
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Getter
    Long projectId;

    @Getter
    Long memberId;

    @Getter
    double investedSum;

    @Getter
    @Setter
    String status = Status.PENDING;

    @Getter
    @Setter
    String membershipScope;

    public ProjectMember(Long projectId, Long memberId,double investedSum, String membershipScope) {
        this.projectId = projectId;
        this.memberId = memberId;
        this.investedSum = investedSum;
        this.membershipScope = membershipScope;
    }


    public ProjectMember(Long projectId, Long memberId, String membershipScope) {
        this.projectId = projectId;
        this.memberId = memberId;
        this.membershipScope = membershipScope;
    }

    public void addInvestedSum(double investedSum){
        this.investedSum += investedSum;
    }
}

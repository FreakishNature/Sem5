package com.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.Status;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name="Projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String description;
    private String owner;
    private double investedSum;
    private double targetSum;

    private String creationDate;
    private String targetDate;

    private String status = Status.PENDING;

    public Project(String name,String description,double targetSum){
        this.name = name;
        this.description = description;
        this.targetSum = targetSum;
    }

    public void invest(double investedSum){
        this.investedSum += investedSum;
    }
}

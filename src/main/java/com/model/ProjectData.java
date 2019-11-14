package com.model;

import com.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectData {
    private String name;
    private String description;
    private String owner;
    private double investedSum;
    private double targetSum;

    private String creationDate;
    private String targetDate;

    public ProjectData(Project project) {
        this.name = project.getName();
        this.description = project.getDescription();
        this.owner = project.getOwner();
        this.investedSum = project.getInvestedSum();
        this.targetSum = project.getTargetSum();
        this.creationDate = project.getCreationDate();
        this.targetDate = project.getTargetDate();
    }
}

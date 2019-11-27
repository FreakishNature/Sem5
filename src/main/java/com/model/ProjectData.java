package com.model;

import com.entities.Project;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectData {
    static ObjectMapper mapper = new ObjectMapper();

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

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "ProjectData{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owner='" + owner + '\'' +
                ", investedSum=" + investedSum +
                ", targetSum=" + targetSum +
                ", creationDate='" + creationDate + '\'' +
                ", targetDate='" + targetDate + '\'' +
                '}';
    }
}

package com.model;

import javax.persistence.*;

@Entity
@Table(name = "ProjectMembers")
public class ProjectMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    Long projectId;
    Long memberId;

}

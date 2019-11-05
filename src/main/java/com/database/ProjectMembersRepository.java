package com.database;

import com.model.ProjectMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMembersRepository extends JpaRepository<ProjectMembers,Long> {
    List<ProjectMembers> findAllByProjectId(long projectId);
}

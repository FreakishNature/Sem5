package com.database;

import com.entities.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMembersRepository extends JpaRepository<ProjectMember,Long> {
    List<ProjectMember> findAllByProjectId(long projectId);
    List<ProjectMember> findAllByProjectIdAndStatus(long projectId,String status);
    List<ProjectMember> findAllByMemberId(long memberId);
    Optional<ProjectMember> findByProjectIdAndMemberId(long projectId, long memberId);

}

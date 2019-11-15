package com.database;

import com.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {
    Optional<Project> findByName(String name);
    List<Project> findAllByOwner(String owner);
    List<Project> findAllByOwnerAndStatus(String owner,String status);
    List<Project> findAllByStatus(String status);
    Optional<Project> findByNameAndStatus(String name,String status);
}

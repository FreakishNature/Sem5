package com.database;

import com.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {
    Project findByName(String name);
    List<Project> findAllByOwner(String owner);
    @Query("update Projects set name = ?2, description = ?3 where name = ?1 ")
    void updateProject(String oldName,String projectName,String description);

}

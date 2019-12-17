package com.database;

import com.entities.Account;
import com.entities.ProjectImages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectImagesRepository extends JpaRepository<ProjectImages,Long> {

}

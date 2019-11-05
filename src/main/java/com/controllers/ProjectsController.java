package com.controllers;

import com.database.AccountRepository;
import com.database.ProjectRepository;
import com.model.Account;
import com.model.Project;
import com.response.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/projects")
@RestController
public class ProjectsController {
    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AccountRepository accountRepository;


    @PostMapping
    ResponseEntity<Object> createProject(@RequestBody Project project) {
        projectRepository.save(project);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{projectName}")
    ResponseEntity<Project> getProjectByName(@PathVariable String projectName){
        return new ResponseEntity<>(projectRepository.findByName(projectName),HttpStatus.OK);
    }

    @GetMapping
    ResponseEntity getAllProjects(){
        return new ResponseEntity<>(projectRepository.findAll(),HttpStatus.OK);
    }

    @GetMapping("/{ownerName}")
    ResponseEntity getAllProjectsForOwner(@PathVariable String ownerName){
        Account account = accountRepository.findFirstByUsername(ownerName);

        if(account == null ){
            return new ResponseEntity<>(ErrorResponse.errorResponseNoSuchUsername(ownerName),HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(projectRepository.findAllByOwner(ownerName),HttpStatus.OK);
    }

    @PutMapping("/{projectName}")
    ResponseEntity updateProject(@PathVariable String projectName,@RequestBody Project project){
        if(projectRepository.findByName(projectName) == null){
//            return new ResponseEntity(new ErrorResponse("N"))
        }
        projectRepository.updateProject(projectName,project.getName(),project.getDescription());
        return null;
    }
}

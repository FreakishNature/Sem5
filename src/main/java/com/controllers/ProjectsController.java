package com.controllers;

import com.database.AccountRepository;
import com.database.ProjectMembersRepository;
import com.database.ProjectRepository;
import com.entities.Account;
import com.entities.Project;
import com.entities.ProjectMember;
import com.model.*;
import com.response.ErrorResponse;
import com.security.Authorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/projects")
@RestController
public class ProjectsController {
    private final
    ProjectRepository projectRepository;

    private final
    AccountRepository accountRepository;

    private final
    ProjectMembersRepository projectMembersRepository;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    public ProjectsController(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }


    @PostMapping
    ResponseEntity createProject(Authentication authentication, @RequestBody Project project) {
        String username = authentication.getPrincipal().toString();

        if(projectRepository.findByName(project.getName()).isPresent()){
            return errorIfProjectNotFound(project.getName());
        }

        project.setOwner(username);
        project.setCreationDate(dateFormat.format(new Date()));
        projectRepository.save(project);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/{projectName}/updateStatus")
    private ResponseEntity<ErrorResponse> updateProjectStatus(
            @PathVariable String projectName,
            @RequestParam Optional<String> status){
        if(!status.isPresent()){
            return new ResponseEntity<>(
                    new ErrorResponse("Required request parameter status is missing"),
                    HttpStatus.BAD_REQUEST
            );
        }

        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return errorIfProjectNotFound(projectName);
        }

        project.get().setStatus(status.get());
        projectRepository.save(project.get());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @GetMapping("/{projectName}")
    ResponseEntity getProjectByName(@PathVariable String projectName){
        Optional<Project> project = projectRepository.findByName(projectName);

        return project.<ResponseEntity>map(p ->
                new ResponseEntity<>(new ProjectData(p), HttpStatus.OK)
                ).orElseGet(() -> errorIfProjectNotFound(projectName)
        );

    }

    @DeleteMapping("/{projectName}")
    ResponseEntity deleteProjectByName(@PathVariable String projectName){
        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return errorIfProjectNotFound(projectName);
        }

        if(project.get().getInvestedSum() > 0){
            return new ResponseEntity<>(
                    new ErrorResponse("You can not delete project which has invested sum"),
                    HttpStatus.BAD_REQUEST
            );
        }

        projectRepository.delete(project.get());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @GetMapping
    ResponseEntity getAllProjects(Authentication auth,
                                 @RequestParam(required = false) Optional<String> status){
        String username = auth.getPrincipal().toString();
        Optional<Account> account = accountRepository.findFirstByUsername(username);

        if(account.get().getRole().equals(Authorities.ADMIN) ||
                account.get().getRole().equals(Authorities.MODERATOR)){

            if(status.isPresent()){
                if(!Status.ALLOWED_STATUSES.contains(status.get())){
                    return noSuchStatusError(status.get());
                }

                return new ResponseEntity<>(
                        projectRepository.findAllByStatus(status.get()),
                        HttpStatus.OK
                );
            }


            return new ResponseEntity<>(
                    projectRepository.findAll(),
                    HttpStatus.OK
            );
        }

        return new ResponseEntity<>(
                projectRepository.findAll()
                        .stream()
                        .map(ProjectData::new)
                        .collect(Collectors.toList()),
                HttpStatus.OK
        );
    }

    @GetMapping("/owners/{ownerName}")
    ResponseEntity getAllProjectsForOwner(Authentication auth,
                                          @PathVariable String ownerName,
                                          @RequestParam(required = false) Optional<String> status){
        String username = auth.getPrincipal().toString();
        Optional<Account> account = accountRepository.findFirstByUsername(ownerName);

        if(!account.isPresent()){
            return new ResponseEntity<>(
                    ErrorResponse.errorResponseNoSuchUsername(ownerName),
                    HttpStatus.NOT_FOUND
            );
        }
        account = accountRepository.findFirstByUsername(username);

        if(account.get().getRole().equals(Authorities.ADMIN) ||
            account.get().getRole().equals(Authorities.MODERATOR) ||
            username.equals(ownerName)){

            if(status.isPresent()){
                if(!Status.ALLOWED_STATUSES.contains(status.get())){
                    return noSuchStatusError(status.get());
                }

                return new ResponseEntity<>(
                        projectRepository.findAllByOwnerAndStatus(ownerName,status.get()),
                        HttpStatus.OK
                );
            }


            return new ResponseEntity<>(
                    projectRepository.findAllByOwner(ownerName),
                    HttpStatus.OK
            );
        }

        if(status.isPresent()){
            return new ResponseEntity<>(
                    new ErrorResponse("You are not allowed to search by status not your projects."),
                    HttpStatus.FORBIDDEN
            );
        }

        return new ResponseEntity<>(
                projectRepository.findAllByOwner(ownerName)
                        .stream()
                        .map(ProjectData::new)
                        .collect(Collectors.toList()),
                HttpStatus.OK
        );
    }

    @PutMapping("/{projectName}")
    ResponseEntity<ErrorResponse> updateProject(@PathVariable String projectName,@RequestBody Project project){
        Optional<Project> searchedProject = projectRepository.findByName(projectName);

        if(!searchedProject.isPresent()){
            return errorIfProjectNotFound(projectName);
        }

        if(projectRepository.findByName(project.getName()).isPresent()){
            return new ResponseEntity<>(
                    ErrorResponse.errorResponseWithExistingField("project name"),
                    HttpStatus.BAD_REQUEST
            );
        }

        searchedProject.get().setName(project.getName());
        searchedProject.get().setDescription(project.getDescription());
        searchedProject.get().setStatus(Status.PENDING);

        projectRepository.save(searchedProject.get());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{projectName}")
    ResponseEntity<ErrorResponse> patchProject(Authentication auth,
                                               @RequestParam double investingSum,
                                               @PathVariable String projectName){
        if(investingSum <= 0){
            return new ResponseEntity<>(
                    new ErrorResponse("You can not invest sum less or equal to 0"),
                    HttpStatus.BAD_REQUEST
            );
        }

        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return errorIfProjectNotFound(projectName);
        }

        Optional<Account> account = accountRepository.findFirstByUsername(auth.getPrincipal().toString());

        project.get().invest(investingSum);
        projectRepository.save(project.get());

        Optional<ProjectMember> projectMemberOptional = projectMembersRepository
                .findByProjectIdAndMemberId(project.get().getId(),account.get().getId());

        ProjectMember projectMember;
        if(!projectMemberOptional.isPresent()){
            projectMember = new ProjectMember(
                    project.get().getId(),
                    account.get().getId(),
                    investingSum,
                    "Investor"
            );

        } else {
            projectMember = projectMemberOptional.get();
            projectMember.addInvestedSum(investingSum);
        }

        projectMember.setStatus(Status.ACCEPTED);
        projectMembersRepository.save(projectMember);


        if(account.get().getRole().equals(Authorities.PROJECT_MEMBER)){
            account.get().setRole(Authorities.INVESTOR);
            accountRepository.save(account.get());

        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    static ResponseEntity<ErrorResponse> errorIfProjectNotFound(String projectName){
        return new ResponseEntity<>(
                    ErrorResponse.projectDoesNotExists(projectName),
                    HttpStatus.NOT_FOUND
            );
    }

    static ResponseEntity<ErrorResponse> noSuchStatusError(String status){
        return new ResponseEntity<>(
                new ErrorResponse("There is no such status : " +
                        status + ". The only allowed statuses are : " + Status.ALLOWED_STATUSES),
                HttpStatus.BAD_REQUEST
        );
    }


}

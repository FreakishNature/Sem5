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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequestMapping("/projects")
@RestController
public class ProjectMembersController {
    private final
    ProjectRepository projectRepository;

    private final
    AccountRepository accountRepository;

    private final
    ProjectMembersRepository projectMembersRepository;

    @Autowired
    public ProjectMembersController(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
    }


    @GetMapping("/{projectName}/members")
    private ResponseEntity getAllProjectMembers(
            Authentication auth,
            @PathVariable String projectName,
            @RequestParam(required = false) Optional<String> status){

        String username = auth.getPrincipal().toString();
        Optional<Account> account = accountRepository.findFirstByUsername(username);

        if(status.isPresent()){
            if(!Status.ALLOWED_STATUSES.contains(status.get())){
                return ProjectsController.noSuchStatusError(status.get());
            }
        }

        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return ProjectsController.errorIfProjectNotFound(projectName);
        }

        if(!projectMembersRepository.findByProjectIdAndMemberId(
                project.get().getId(),
                account.get().getId()).isPresent() || !account.get().getRole().equals(Authorities.ADMIN)){
            return new ResponseEntity<>(
                    new ErrorResponse("You do not have permissions to view members of this project."),
                    HttpStatus.FORBIDDEN
            );
        }

        List<ProjectMember> projectMembers = !status.isPresent() ? projectMembersRepository.findAllByProjectId(project.get().getId())
                : projectMembersRepository.findAllByProjectIdAndStatus(project.get().getId(),status.get());

        ArrayList<ProjectMemberData> projectMembersData = new ArrayList<>();

        projectMembers.forEach( member -> {
            Account searchedAccount = accountRepository.findById(member.getMemberId()).get();
            if(project.get().getOwner().equals(username)){
                projectMembersData.add(
                        new ProjectMemberData(
                                searchedAccount.getUsername(),
                                searchedAccount.getRole(),
                                member.getInvestedSum(),
                                member.getStatus()
                        )
                );
            } else {
                projectMembersData.add(
                        new ProjectMemberData(
                                searchedAccount.getUsername(),
                                searchedAccount.getRole(),
                                member.getInvestedSum()
                        )
                );
            }
        });

        return new ResponseEntity<>(projectMembersData,HttpStatus.OK);
    }

    @PostMapping("/{projectName}/members/join")
    private ResponseEntity<ErrorResponse> joinProject(Authentication auth,
                                                      @PathVariable String projectName,
                                                      @RequestBody JoiningRequest joiningRequest){
        String username = auth.getPrincipal().toString();

        Optional<Account> account = accountRepository.findFirstByUsername(username);

        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return ProjectsController.errorIfProjectNotFound(projectName);
        }
        Optional<ProjectMember> projectMember = projectMembersRepository.findById(account.get().getId());

        if(projectMember.isPresent()){
            if(projectMember.get().getStatus().equals(Status.REJECTED)){
                return new ResponseEntity<>(
                        new ErrorResponse("You are not allowed to join this project as its member," +
                                " but you still can be an investor of this project"),
                        HttpStatus.CONFLICT
                );
            }else {
                return new ResponseEntity<>(
                        new ErrorResponse("You are already member of this project"),
                        HttpStatus.CONFLICT
                );
            }
        }

        projectMembersRepository.save(new ProjectMember(
                project.get().getId(),
                account.get().getId(),
                joiningRequest.getMembershipScope()
        ));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{projectName}/members/{memberName}")
    ResponseEntity<ErrorResponse> updateUserStatus(Authentication auth,
                                    @PathVariable String projectName,
                                    @PathVariable String memberName,
                                    @RequestParam String status){

        if(!Status.ALLOWED_STATUSES.contains(status)){
            return ProjectsController.noSuchStatusError(status);
        }

        String username = auth.getPrincipal().toString();


        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return ProjectsController.errorIfProjectNotFound(projectName);
        }

        if(!project.get().getOwner().equals(username)){
            return new ResponseEntity<>(
                    new ErrorResponse("You do not have permissions to modify users of this project"),
                    HttpStatus.FORBIDDEN
            );
        }
        Optional<Account> membersAccount = accountRepository.findFirstByUsername(memberName);

        if(!membersAccount.isPresent()){
            return new ResponseEntity<>(
                    ErrorResponse.errorResponseNoSuchUsername(memberName),
                    HttpStatus.NOT_FOUND
            );
        }

        Optional<ProjectMember> projectMember = projectMembersRepository.findByProjectIdAndMemberId(
                project.get().getId(),
                membersAccount.get().getId()
        );

        if(!projectMember.isPresent()){
            return new ResponseEntity<>(
                    new ErrorResponse("User " + memberName + " is not part of this project"),
                    HttpStatus.NOT_FOUND
            );
        }

        projectMember.get().setStatus(status);

        projectMembersRepository.save(projectMember.get());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }



}

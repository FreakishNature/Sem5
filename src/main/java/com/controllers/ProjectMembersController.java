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
import com.utils.JsonUtils;
import org.apache.log4j.Logger;
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
    private static Logger log = Logger.getLogger(ProjectMembersController.class);
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


    /*
        Getting list of project members.

        * Only MEMBERS of this PROJECT, ADMIN, OWNER of this PROJECT can access
        this endpoint for others should be returned 403

        * If status was specified and user has access to view status should be checked if it is valid

        * If project does not exists should be returned 404

        * If user has access to see status should be returned data with status.
     */
    @GetMapping("/{projectName}/members")
    private ResponseEntity getAllProjectMembers(
            Authentication auth,
            @PathVariable String projectName,
            @RequestParam(required = false) Optional<String> status){

        log.info("GET /projects/" + projectName + "/members");

        String username = auth.getPrincipal().toString();
        Optional<Account> account = accountRepository.findFirstByUsername(username);
        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return responseWithLogs(ProjectsController.errorIfProjectNotFound(projectName));
        }

        boolean userHasAccessToViewStatus =
                project.get().getOwner().equals(username) ||
                        account.get().getRole().equals(Authorities.ADMIN) ||
                        account.get().getRole().equals(Authorities.MODERATOR);


        if(status.isPresent() && userHasAccessToViewStatus){
            if(!Status.ALLOWED_STATUSES.contains(status.get())){
                return responseWithLogs(ProjectsController.noSuchStatusError(status.get()));
            }
        }


        if(!projectMembersRepository.findByProjectIdAndMemberId(
                project.get().getId(),
                account.get().getId()).isPresent() &&
                !account.get().getRole().equals(Authorities.ADMIN) &&
                !account.get().getRole().equals(Authorities.MODERATOR)){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("You do not have permissions to view members of this project."),
                            HttpStatus.FORBIDDEN
                    )
            );
        }

        List<ProjectMember> projectMembers = !status.isPresent() && userHasAccessToViewStatus? projectMembersRepository.findAllByProjectId(project.get().getId())
                : projectMembersRepository.findAllByProjectIdAndStatus(project.get().getId(),status.get());

        ArrayList<ProjectMemberData> projectMembersData = new ArrayList<>();

        projectMembers.forEach( member -> {
            Account searchedAccount = accountRepository.findById(member.getMemberId()).get();
            if(userHasAccessToViewStatus){

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

        return responseWithLogs(new ResponseEntity<>(projectMembersData,HttpStatus.OK));
    }

    /*
        Joining project endpoint.

        * If project does not exists should be returned 404

        * If your status is REJECTED or you are already member of this endpoint you should get 409

     */

    @PostMapping("/{projectName}/members/join")
    private ResponseEntity joinProject(Authentication auth,
                                                      @PathVariable String projectName,
                                                      @RequestBody JoiningRequest joiningRequest){
        log.info("POST /projects/" + projectName + "/members/join request body : " + JsonUtils.toJsonString(joiningRequest));

        String username = auth.getPrincipal().toString();
        Optional<Account> account = accountRepository.findFirstByUsername(username);
        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return responseWithLogs(ProjectsController.errorIfProjectNotFound(projectName));
        }

        Optional<ProjectMember> projectMember = projectMembersRepository.findById(account.get().getId());

        if(projectMember.isPresent()){
            if(projectMember.get().getStatus().equals(Status.REJECTED)){
                return responseWithLogs(
                        new ResponseEntity<>(
                                new ErrorResponse("You are not allowed to join this project as its member," +
                                        " but you still can be an investor of this project"),
                                HttpStatus.CONFLICT
                        )
                );
            }else {
                return responseWithLogs(
                        new ResponseEntity<>(
                                new ErrorResponse("You are already member of this project"),
                                HttpStatus.CONFLICT
                        )
                );
            }
        }

        projectMembersRepository.save(new ProjectMember(
                project.get().getId(),
                account.get().getId(),
                joiningRequest.getMembershipScope()
        ));

        return responseWithLogs(new ResponseEntity<>(HttpStatus.OK));
    }

    /*
        Updating member status

        * Status is required request parameter

        * If status is not valid should be returned 400

        * If project does not exists should be returned 404

        * To modify member's status are allowed only OWNER of this PROJECT and ADMIN

        * If specified member does not exists should be returned 404

        * Status of investors can not be changed
     */
    @PatchMapping("/{projectName}/members/{memberName}")
    ResponseEntity updateUserStatus(Authentication auth,
                                    @PathVariable String projectName,
                                    @PathVariable String memberName,
                                    @RequestParam String status){
        callingEndpointLog("PATCH /projects/" + projectName + "/members/" + memberName + "?status?=" + status);
        if(!Status.ALLOWED_STATUSES.contains(status)){
            return responseWithLogs(ProjectsController.noSuchStatusError(status));
        }

        String username = auth.getPrincipal().toString();
        Account account = accountRepository.findFirstByUsername(username).get();
        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return responseWithLogs(ProjectsController.errorIfProjectNotFound(projectName));
        }

        if(!project.get().getOwner().equals(username) &&
            !account.getRole().equals(Authorities.ADMIN)){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("You do not have permissions to modify users of this project"),
                            HttpStatus.FORBIDDEN
                    )
            );
        }
        Optional<Account> membersAccount = accountRepository.findFirstByUsername(memberName);

        if(!membersAccount.isPresent()){
            return responseWithLogs(
                    new ResponseEntity<>(
                            ErrorResponse.errorResponseNoSuchUsername(memberName),
                            HttpStatus.NOT_FOUND
                    )
            );
        }

        Optional<ProjectMember> projectMember = projectMembersRepository.findByProjectIdAndMemberId(
                project.get().getId(),
                membersAccount.get().getId()
        );

        if(!projectMember.isPresent()){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("User " + memberName + " is not part of this project"),
                            HttpStatus.NOT_FOUND
                    )
            );
        }

        if(projectMember.get().getMembershipScope().equals("Investor")){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("User " + memberName + " is investor and its status can not be changed."),
                            HttpStatus.FORBIDDEN
                    )
            );
        }

        projectMember.get().setStatus(status);
        projectMembersRepository.save(projectMember.get());

        return responseWithLogs(new ResponseEntity<>(HttpStatus.ACCEPTED));
    }

    private ResponseEntity responseWithLogs(ResponseEntity responseEntity){
        log.info("Has been returned response : " + responseEntity);
        return responseEntity;
    }

    private void callingEndpointLog(String endpoint){
        log.info("Has been called endpoint " + endpoint);
    }


}

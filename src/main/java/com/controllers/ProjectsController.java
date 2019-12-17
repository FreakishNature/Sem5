package com.controllers;

import com.database.AccountRepository;
import com.database.CategoryRepository;
import com.database.ProjectMembersRepository;
import com.database.ProjectRepository;
import com.entities.Account;
import com.entities.Category;
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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// Create validations for all request bodies and set all fields which should not be putted to null

@RequestMapping("/projects")
@RestController
public class ProjectsController {
    private static Logger log = Logger.getLogger(ProjectsController.class);

    private final
    ProjectRepository projectRepository;

    private final
    AccountRepository accountRepository;

    private final
    ProjectMembersRepository projectMembersRepository;

    private final CategoryRepository categoryRepository;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Map<Long,String> categoriesMap;

    public String getCategory(long id){
        if(categoriesMap == null){
            categoriesMap = new HashMap<>();
            List<Category> categories = categoryRepository.findAll();

            for(Category category : categories){
                categoriesMap.put(category.getId(),category.getCategory());
            }

        }
        return categoriesMap.get(id);
    }

    @Autowired
    public ProjectsController(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMembersRepository projectMembersRepository, CategoryRepository categoryRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMembersRepository = projectMembersRepository;
        this.categoryRepository = categoryRepository;
    }


    /*
           This is endpoint is used to create new projects.

           * Project can not be created if there exists projects with identical name,
           should be returned 409 status code.

           * The owner of this project will become user who is logged in using auth token.

           * After project creation its status becomes PENDING and it could be updated only
           by moderators or admin.

           * After project creation should be set creation date.

     */

    @PostMapping
    ResponseEntity createProject(Authentication authentication, @RequestBody Project project) {
        callingEndpointLog("POST /projects endpoint with request body : " + JsonUtils.toJsonString(project));

        String username = authentication.getPrincipal().toString();

        if(projectRepository.findByName(project.getName()).isPresent()){
            log.debug("Project with name : " + project.getName() + " already exists.");
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("Project with name " + project.getName() + " already exists."),
                            HttpStatus.CONFLICT
                    )
            );
        }

        project.setOwner(username);
        projectRepository.save(project);

        return responseWithLogs(new ResponseEntity<>(HttpStatus.CREATED));
    }

    /*
        This endpoint is used to update project status after its creation.

        * Only MODERATOR and ADMIN should be able to access this endpoint.

        * If project was not found should be returned 404.

        * After changing the status of the project from PENDING to ACCEPTED should be
        set creation time.

        * Project can not be set to PENDING status. Should be returned 400.

     */
    @PatchMapping("/{projectName}/status")
    private ResponseEntity updateProjectStatus(
            @PathVariable String projectName,
            @RequestParam Optional<String> status){
        callingEndpointLog("PATCH /projects/" + projectName + "/status?status=" + status + " endpoint.");

        if(!status.isPresent()){
            log.debug("Required request parameter status is missing");
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("Required request parameter status is missing"),
                            HttpStatus.BAD_REQUEST
                    )
            );
        }

        if(status.get().equals(Status.PENDING)){
            log.debug("Project can not be set to " + Status.PENDING + " status.");
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("Project can not be set to " + Status.PENDING + " status."),
                            HttpStatus.BAD_REQUEST
                    )
            );
        }

        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return responseWithLogs(errorIfProjectNotFound(projectName));
        }

        project.get().setStatus(status.get());

        if(project.get().getCreationDate() == null && status.get().equals(Status.ACCEPTED)){
            project.get().setCreationDate(dateFormat.format(new Date()));
        }

        projectRepository.save(project.get());
        log.debug("Project " + projectName + " status has been updated.");
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /*
        Getting data about single project.

        * If requested by MODERATOR or ADMIN or OWNER of this PROJECT
        data should be returned with status.

        * If project was not found should be returned 404
     */

    @GetMapping("/{projectName}")
    ResponseEntity getProjectByName(
            Authentication auth,
            @PathVariable String projectName){
        callingEndpointLog("GET /projects/" + projectName + " endpoint");

        Optional<Project> project = projectRepository.findByName(projectName);

        if (!project.isPresent()){
            return responseWithLogs(errorIfProjectNotFound(projectName));
        }

        if(auth != null){
            String username = auth.getPrincipal().toString();

            Optional<Account> account = accountRepository.findFirstByUsername(username);
            if(account.get().getRole().equals(Authorities.ADMIN) ||
                account.get().getRole().equals(Authorities.MODERATOR) ||
                account.get().getUsername().equals(project.get().getOwner())){

                return responseWithLogs(new ResponseEntity<>(project.get(), HttpStatus.OK));
            }
        }

        return responseWithLogs(new ResponseEntity<>(new ProjectData(
                project.get(),
                getCategory(project.get().getCategoryId())
        ), HttpStatus.OK));
    }

    /*
        Delete single project.

        * Allowed only to ADMIN and OWNER of this PROJECT.

        * If project was not found should be returned 404

        * If project has invested sum it can be deleted only by ADMIN should be returned 403
     */

    @DeleteMapping("/{projectName}")
    ResponseEntity deleteProjectByName(
            Authentication auth,
            @PathVariable String projectName){
        callingEndpointLog("DELETE /projects/" + projectName);
        String username = auth.getPrincipal().toString();
        Account account = accountRepository.findFirstByUsername(username).get();
        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return responseWithLogs(errorIfProjectNotFound(projectName));
        }

        if(project.get().getInvestedSum() > 0 && !account.getRole().equals(Authorities.ADMIN)){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("You can not delete project which has invested sum"),
                            HttpStatus.FORBIDDEN
                    )
            );
        }

        projectRepository.delete(project.get());

        return responseWithLogs(new ResponseEntity<>(HttpStatus.ACCEPTED));
    }


        /*
        Getting all project.
        We can specify status of projects which we are looking for,
        this functionality is available only for ADMIN and MODERATOR

        If status does not exists should be returned for ADMIN and MODERATOR 400.

        OTHER users should receive response without status field.


     */
    @GetMapping
    ResponseEntity getAllProjects(Authentication auth,
                                 @RequestParam(required = false) Optional<String> status){
        callingEndpointLog("GET /projects?status=" + status);

        String username = auth != null ? auth.getPrincipal().toString() : "";

        Optional<Account> account = accountRepository.findFirstByUsername(username);
        if(account.isPresent()){
            if(account.get().getRole().equals(Authorities.ADMIN) ||
                    account.get().getRole().equals(Authorities.MODERATOR)){

                if(status.isPresent()){
                    if(!Status.ALLOWED_STATUSES.contains(status.get())){
                        return responseWithLogs(noSuchStatusError(status.get()));
                    }

                    return responseWithLogs(
                            new ResponseEntity<>(
                                    projectRepository.findAllByStatus(status.get()),
                                    HttpStatus.OK
                            )
                    );
                }


                return responseWithLogs(
                        new ResponseEntity<>(
                                projectRepository.findAll(),
                                HttpStatus.OK
                        )
                );
            }
        }

        return responseWithLogs(
                new ResponseEntity<>(
                        projectRepository.findAll()
                                .stream()
                                .filter(p -> p.getStatus().equals(Status.ACCEPTED))
                                .map( p -> new ProjectData(
                                        p,
                                        getCategory(p.getCategoryId()))
                                )
                                .collect(Collectors.toList()),
                        HttpStatus.OK
                )
        );
    }

    /*
        Getting all project for specified owner.
        We can specify status of projects which we are looking for,
        this functionality is available only for:
        1) project owners
        2) admin
        3) moderators

        Project owner can view status only of their projects.

        Everyone else should get 403 response code while trying to access
        this endpoint with specified status.

        OTHER users should receive response without status field.

        In case if owner does not exists should be returned 404 error.

     */

    @GetMapping("/owners/{ownerName}")
    ResponseEntity getAllProjectsForOwner(Authentication auth,
                                          @PathVariable String ownerName,
                                          @RequestParam(required = false) Optional<String> status){
        callingEndpointLog("GET /projects/owners/" + ownerName + "?status=" + status);
        String username = auth.getPrincipal().toString();
        Optional<Account> account = accountRepository.findFirstByUsername(ownerName);

        if(!account.isPresent()){
            return responseWithLogs(
                    new ResponseEntity<>(
                            ErrorResponse.errorResponseNoSuchUsername(ownerName),
                            HttpStatus.NOT_FOUND
                    )
            );
        }
        account = accountRepository.findFirstByUsername(username);

        if(account.get().getRole().equals(Authorities.ADMIN) ||
            account.get().getRole().equals(Authorities.MODERATOR) ||
            username.equals(ownerName)){

            if(status.isPresent()){
                if(!Status.ALLOWED_STATUSES.contains(status.get())){
                    return responseWithLogs(noSuchStatusError(status.get()));
                }

                return responseWithLogs(
                        new ResponseEntity<>(
                                projectRepository.findAllByOwnerAndStatus(ownerName,status.get()),
                                HttpStatus.OK
                        )
                );
            }


            return responseWithLogs(
                    new ResponseEntity<>(
                            projectRepository.findAllByOwner(ownerName),
                            HttpStatus.OK
                    )
            );
        }

        if(status.isPresent()){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("You are not allowed to search by status foreign projects."),
                            HttpStatus.FORBIDDEN
                    )
            );
        }

        return responseWithLogs(
                new ResponseEntity<>(
                        projectRepository.findAllByOwner(ownerName)
                                .stream()
                                .map( p -> new ProjectData(
                                        p,
                                        getCategory(p.getCategoryId()))
                                )
                                .collect(Collectors.toList()),
                        HttpStatus.OK
                )
        );
    }

    /*
        This endpoint is used to update project name and description.

        * If project does not exists should be returned 404

        * Only OWNER of this PROJECT, MODERATOR, ADMIN can access this endpoint

        * After updating project status should be changed to PENDING
     */

    @PutMapping("/{projectName}")
    ResponseEntity updateProject(
            Authentication auth,
            @PathVariable String projectName,
            @RequestBody Project project){
        callingEndpointLog("PUT /projects/" + projectName + " request body " + JsonUtils.toJsonString(project));

        String username = auth.getPrincipal().toString();
        Account account = accountRepository.findFirstByUsername(username).get();
        Optional<Project> searchedProject = projectRepository.findByName(projectName);

        if(!searchedProject.isPresent()){
            return responseWithLogs(errorIfProjectNotFound(projectName));
        }

        if(projectRepository.findByName(project.getName()).isPresent()){
            return responseWithLogs(
                    new ResponseEntity<>(
                            ErrorResponse.errorResponseWithExistingField("project name"),
                            HttpStatus.BAD_REQUEST
                    )
            );
        }

        if(!account.getRole().equals(Authorities.ADMIN) &&
            !account.getRole().equals(Authorities.MODERATOR) &&
            !searchedProject.get().getOwner().equals(username)){

            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("You do not have permissions to update this resource"),
                            HttpStatus.FORBIDDEN
                    )
            );
        }

        searchedProject.get().setName(project.getName());
        searchedProject.get().setDescription(project.getDescription());
        searchedProject.get().setStatus(Status.PENDING);

        projectRepository.save(searchedProject.get());

        return responseWithLogs(
                new ResponseEntity<>(HttpStatus.ACCEPTED)
        );
    }

    /*
        This endpoint is used for investing to project.

        * Investment could not be negative or 0.

        * If project does not exists should be returned 404.

        * If user was not member of invested project it should be added to the list as Investor
         and if its role was PROJECT_MEMBER it should be changed to Investor.

         * After investing should be added investment sum both to project and to member.

     */

    @PatchMapping("/{projectName}")
    ResponseEntity patchProject(Authentication auth,
                                               @RequestParam double investingSum,
                                               @PathVariable String projectName){
        callingEndpointLog("PATCH /projects/" + projectName + "?investingSum=" + investingSum);

        if(investingSum <= 0){
            return responseWithLogs(
                    new ResponseEntity<>(
                            new ErrorResponse("You can not invest sum less or equal to 0"),
                            HttpStatus.BAD_REQUEST
                    )
            );
        }

        Optional<Project> project = projectRepository.findByName(projectName);

        if(!project.isPresent()){
            return responseWithLogs(errorIfProjectNotFound(projectName));
        }

        if(!project.get().getStatus().equals(Status.ACCEPTED)){
            return responseWithLogs(new ResponseEntity<>(
                    new ErrorResponse("You can not invest to project which status is not active"),
                    HttpStatus.FORBIDDEN
            ));
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

        account.get().addInvestedSum(investingSum);
        accountRepository.save(account.get());
        projectMembersRepository.save(projectMember);


        if(account.get().getRole().equals(Authorities.PROJECT_MEMBER)){
            account.get().setRole(Authorities.INVESTOR);
            accountRepository.save(account.get());

        }

        return responseWithLogs(new ResponseEntity<>(HttpStatus.ACCEPTED));
    }

    @GetMapping("/categories")
    public ResponseEntity getAllCategories(){
        callingEndpointLog("Get /categories");

        return responseWithLogs(new ResponseEntity<>(
                categoryRepository.findAll()
                        .stream()
                        .map(Category::getCategory)
                        .collect(Collectors.toList()),
                HttpStatus.OK)
        );
    }

    @GetMapping("/{projectName}/role")
    public ResponseEntity getProjectRole(
            Authentication auth,
            @PathVariable String projectName
    ){
        callingEndpointLog("PATCH /projects/" + projectName + "/role");

        Optional<Project> project = projectRepository.findByName(projectName);
        if(!project.isPresent()){
            return errorIfProjectNotFound(projectName);
        }

        if(auth == null){
            return responseWithLogs(new ResponseEntity<>(
                    new RoleResponse(RoleResponse.Roles.VIEWER)
                    ,HttpStatus.OK
            ));
        }

        String username = auth.getPrincipal().toString();
        Account account = accountRepository.findFirstByUsername(username).get();

        if(account.getRole().equals(Authorities.MODERATOR) ||
            account.getRole().equals(Authorities.ADMIN) ||
            account.getUsername().equals(project.get().getOwner())){
            return responseWithLogs(new ResponseEntity<>(
                    new RoleResponse(RoleResponse.Roles.ADMIN)
                    ,HttpStatus.OK
            ));
        }

        Optional<ProjectMember> member = projectMembersRepository.findByProjectIdAndMemberId(
                project.get().getId(),
                account.getId()
        );

        if(member.isPresent()){
            return responseWithLogs(new ResponseEntity<>(
                    new RoleResponse(RoleResponse.Roles.MEMBER)
                    ,HttpStatus.OK
            ));
        }

        return responseWithLogs(new ResponseEntity<>(
                new RoleResponse(RoleResponse.Roles.VIEWER)
                ,HttpStatus.OK
        ));
    }

    static ResponseEntity<ErrorResponse> errorIfProjectNotFound(String projectName){
        log.info("Project with project name " + projectName + " does not exists.");
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

    private ResponseEntity responseWithLogs(ResponseEntity responseEntity){
        log.info("Has been returned response : " + responseEntity);
        return responseEntity;
    }

    private void callingEndpointLog(String endpoint){
        log.info("Has been called endpoint " + endpoint);
    }
}

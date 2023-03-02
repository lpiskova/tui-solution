package com.tuigroup.tuihomework.controllers;

import com.tuigroup.tuihomework.model.Branch;
import com.tuigroup.tuihomework.services.BranchService;
import com.tuigroup.tuihomework.services.RepositoryService;
import com.tuigroup.tuihomework.dto.ErrorMessage;
import com.tuigroup.tuihomework.dto.RepositoryDto;
import com.tuigroup.tuihomework.services.RepositoryMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Github Repository API")
@RequiredArgsConstructor
public class RepositoryController {

    public static final String USER_REPOSITORIES_URL = "/users/{username}/repositories";

    private final RepositoryService repositoryService;

    private final BranchService branchService;

    private final RepositoryMapper repositoryMapper;

    @GetMapping(value = USER_REPOSITORIES_URL, headers = "Accept=application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username found",
                    content = {@Content(schema = @Schema(implementation = RepositoryDto.class))}),
            @ApiResponse(responseCode = "404", description = "Username not found",
                    content = {@Content(schema = @Schema(implementation = ErrorMessage.class))}),
            @ApiResponse(responseCode = "406", description = "Not acceptable content type supplied",
                    content = {@Content(schema = @Schema(implementation = ErrorMessage.class))})}
    )
    public List<RepositoryDto> retrieveReposByUser(@PathVariable("username") String user) {
        return repositoryService.getNotForkRepositories(user)
                .parallelStream()
                .map(repository -> {
                    List<Branch> branches = branchService.getBranches(user, repository.getName());
                    return repositoryMapper.toRepositoryDto(repository, branches);
                })
                .toList();
    }

}
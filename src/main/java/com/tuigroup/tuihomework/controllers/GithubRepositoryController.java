package com.tuigroup.tuihomework.controllers;

import com.tuigroup.tuihomework.exception.UserNotFoundException;
import com.tuigroup.tuihomework.services.GithubService;
import com.tuigroup.tuihomework.view.ErrorMessage;
import com.tuigroup.tuihomework.view.Repository;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Tag(name = "Github Repository API")
@RequiredArgsConstructor
public class GithubRepositoryController {

    private final GithubService githubService;

    @RequestMapping(value = "/repos/{username}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username found",
                    content = {@Content(schema = @Schema(implementation = Repository.class))}),
            @ApiResponse(responseCode = "404", description = "Username not found",
                    content = {@Content(schema = @Schema(implementation = ErrorMessage.class))}),
            @ApiResponse(responseCode = "406", description = "Not acceptable content type supplied",
                    content = {@Content(schema = @Schema(implementation = ErrorMessage.class))})}
    )
    public List<Repository> retrieveReposByUser(@PathVariable("username") String user) {
        try {
            return githubService.getNotForkRepositories(user);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
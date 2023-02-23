package com.tuigroup.tuihomework.controllers;

import com.tuigroup.tuihomework.exception.UserNotFoundException;
import com.tuigroup.tuihomework.services.GithubService;
import com.tuigroup.tuihomework.view.Repository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GithubRepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private GithubService githubService;

    @Test
    public void retrieveUserRepositories_success() throws Exception {
        List<Repository> repositories = List.of(
                new Repository("repo1", "owner1", List.of()),
                new Repository("repo2", "owner2", List.of()),
                new Repository("repo3", "owner3", List.of()));

        Mockito.when(githubService.getNotForkRepositories("user")).thenReturn(repositories);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/repos/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("repo1")));
    }

    @Test
    public void retrieveUserRepositories_successWithNoRepo() throws Exception {
        Mockito.when(githubService.getNotForkRepositories("user")).thenReturn(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/repos/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void retrieveUserRepositories_failureWithNotFound() throws Exception {
        Mockito.when(githubService.getNotForkRepositories("user")).thenThrow(new UserNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/repos/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage(), containsString(HttpStatus.NOT_FOUND.toString())));
    }

    @Test
    public void retrieveUserRepositories_failureWithUnexpectedError() throws Exception {
        Mockito.when(githubService.getNotForkRepositories("user")).thenThrow(new RuntimeException("Internal Error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/repos/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage(), containsString(HttpStatus.INTERNAL_SERVER_ERROR.toString())));
    }

}

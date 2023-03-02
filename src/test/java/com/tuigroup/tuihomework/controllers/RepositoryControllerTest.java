package com.tuigroup.tuihomework.controllers;

import com.tuigroup.tuihomework.model.Branch;
import com.tuigroup.tuihomework.model.Commit;
import com.tuigroup.tuihomework.model.Owner;
import com.tuigroup.tuihomework.model.Repository;
import com.tuigroup.tuihomework.services.BranchService;
import com.tuigroup.tuihomework.services.RepositoryMapper;
import com.tuigroup.tuihomework.services.RepositoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepositoryController.class)
public class RepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    RepositoryService repositoryService;

    @MockBean
    BranchService branchService;

    @MockBean
    RepositoryMapper repositoryMapper;

    @Captor
    private ArgumentCaptor<String> captor;

    private final String user = "user";

    @Test
    public void retrieveUserRepositories_success() throws Exception {
        Repository repo1 = new Repository("repo1", new Owner("owner1"), false);
        Repository repo2 = new Repository("repo2", new Owner("owner2"), false);
        Repository repo3 = new Repository("repo3", new Owner("owner3"), false);

        List<Repository> repositories = List.of(repo1, repo2, repo3);

        Branch branch = new Branch("branch", new Commit("commit"));

        given(repositoryService.getNotForkRepositories(user)).willReturn(repositories);
        given(branchService.getBranches(user, repo1.getName())).willReturn(List.of(branch));
        given(branchService.getBranches(user, repo2.getName())).willReturn(List.of(branch));
        given(branchService.getBranches(user, repo3.getName())).willReturn(List.of(branch));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(RepositoryController.USER_REPOSITORIES_URL, user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void retrieveUserRepositories_successWithNoRepositories() throws Exception {
        given(repositoryService.getNotForkRepositories(user)).willReturn(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(RepositoryController.USER_REPOSITORIES_URL, user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void retrieveUserRepositories_failureWithNotFound() throws Exception {
        given(repositoryService.getNotForkRepositories(user)).willThrow(
                HttpClientErrorException.create(
                        HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()),
                        "",
                        new HttpHeaders(),
                        new byte[]{},
                        StandardCharsets.UTF_8
                )
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .get(RepositoryController.USER_REPOSITORIES_URL, user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);

        verify(repositoryService).getNotForkRepositories(usernameCaptor.capture());
        assertEquals(user, usernameCaptor.getValue());
    }

    @Test
    public void retrieveUserRepositories_failureWithUnexpectedError() throws Exception {
        given(repositoryService.getNotForkRepositories(user)).willThrow(new RuntimeException("Internal Error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(RepositoryController.USER_REPOSITORIES_URL, user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(repositoryService).getNotForkRepositories(captor.capture());
        assertEquals(user, captor.getValue());
    }

}

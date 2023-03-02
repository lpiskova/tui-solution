package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.dto.RepositoryDto;
import com.tuigroup.tuihomework.model.Owner;
import com.tuigroup.tuihomework.model.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class RepositoryServiceTest {

    @InjectMocks
    private GithubRepositoryService repositoryService;

    @Mock
    private GithubClient githubClient;

    private final String user = "user";

    @Test
    public void getUserRepositories_success() {
        Owner owner = new Owner(user);
        List<Repository> repositories = List.of(
                new Repository("repo1", owner, true),
                new Repository("repo2", owner, false),
                new Repository("repo3", owner, false),
                new Repository("repo4", owner, false));

        Mockito.when(githubClient.getUserRepositories((user))).thenReturn(repositories);
        assertEquals(3, repositoryService.getNotForkRepositories(user).size());
    }

    @Test
    public void getUserRepositories_successWithNoRepositories() {
        Mockito.when(githubClient.getUserRepositories((user))).thenReturn(List.of());
        assertEquals(0, repositoryService.getNotForkRepositories(user).size());
    }

    @Test
    public void getUserRepositories_success_compareResults() {
        Owner owner = new Owner(user);
        String repositoryName = "repo";
        List<Repository> repositories = List.of(new Repository(repositoryName, owner, false));

        Mockito.when(githubClient.getUserRepositories((user))).thenReturn(repositories);

        List<RepositoryDto> expected = List.of(new RepositoryDto(repositoryName, owner.getLogin(), List.of()));
        List<Repository> actual = repositoryService.getNotForkRepositories(user);

        assertEquals(actual.get(0).getName(), expected.get(0).getName());
        assertEquals(actual.get(0).getOwner().getLogin(), expected.get(0).getOwnerLogin());
    }

    @Test
    public void getUserRepositories_failure() {
        Mockito.when(githubClient.getUserRepositories((user))).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> repositoryService.getNotForkRepositories(user));
    }
}

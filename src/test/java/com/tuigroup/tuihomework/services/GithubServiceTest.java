package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.model.GithubBranch;
import com.tuigroup.tuihomework.model.GithubCommit;
import com.tuigroup.tuihomework.model.GithubOwner;
import com.tuigroup.tuihomework.model.GithubRepository;
import com.tuigroup.tuihomework.view.Branch;
import com.tuigroup.tuihomework.view.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GithubServiceTest {

    @InjectMocks
    private GithubServiceImpl githubService;

    @Mock
    private GithubClient githubClient;

    @Test
    public void getOnlyNotForkRepositories_success() {
        GithubOwner owner = new GithubOwner("user");
        List<GithubRepository> repositories = List.of(
                new GithubRepository("repo1", owner, true),
                new GithubRepository("repo2", owner, false),
                new GithubRepository("repo3", owner, false),
                new GithubRepository("repo4", owner, false));

        Mockito.when(githubClient.getUserRepositories(("user"))).thenReturn(repositories);
        assertEquals(3, githubService.getNotForkRepositories("user").size());
    }

    @Test
    public void getOnlyNotForkRepositories_successWithNoRepo() {
        GithubOwner owner = new GithubOwner("user");
        List<GithubRepository> repositories = List.of(
                new GithubRepository("repo1", owner, true),
                new GithubRepository("repo2", owner, true));

        Mockito.when(githubClient.getUserRepositories(("user"))).thenReturn(repositories);
        assertEquals(0, githubService.getNotForkRepositories("user").size());
    }

    @Test
    public void getNotForkRepositories_successWithBranches() {
        GithubOwner owner = new GithubOwner("user");
        List<GithubRepository> repositories = List.of(
                new GithubRepository("repo1", owner, true),
                new GithubRepository("repo2", owner, false),
                new GithubRepository("repo3", owner, false),
                new GithubRepository("repo4", owner, false));
        List<GithubBranch> branchesRepo2 = List.of(
                new GithubBranch("master", new GithubCommit("aa")),
                new GithubBranch("branch1", new GithubCommit("ab")),
                new GithubBranch("branch2", new GithubCommit("ac"))
        );
        List<GithubBranch> branchesRepo3 = List.of(new GithubBranch("master", new GithubCommit("ba")));
        List<GithubBranch> branchesRepo4 = List.of(new GithubBranch("master", new GithubCommit("ca")));

        Mockito.when(githubClient.getUserRepositories(("user"))).thenReturn(repositories);
        Mockito.when(githubClient.getUserRepositoryBranches("user", "repo2")).thenReturn(branchesRepo2);
        Mockito.when(githubClient.getUserRepositoryBranches("user", "repo3")).thenReturn(branchesRepo3);
        Mockito.when(githubClient.getUserRepositoryBranches("user", "repo4")).thenReturn(branchesRepo4);

        List<Repository> actual = githubService.getNotForkRepositories("user");
        assertEquals(3, actual.size());
    }

    @Test
    public void getNotForkRepositories_successWithBranches_compareResults() {
        GithubOwner owner = new GithubOwner("user");
        List<GithubRepository> repositories = List.of(
                new GithubRepository("repo1", owner, true),
                new GithubRepository("repo2", owner, false));
        List<GithubBranch> branchesRepo2 = List.of(
                new GithubBranch("master", new GithubCommit("aa")),
                new GithubBranch("branch1", new GithubCommit("ab")),
                new GithubBranch("branch2", new GithubCommit("ac"))
        );

        Mockito.when(githubClient.getUserRepositories(("user"))).thenReturn(repositories);
        Mockito.when(githubClient.getUserRepositoryBranches("user", "repo2")).thenReturn(branchesRepo2);

        List<Repository> expected = List.of(
                new Repository("repo2", owner.getLogin(), List.of(
                        new Branch("master", "aa"),
                        new Branch("branch1", "ab"),
                        new Branch("branch2", "ac")))
        );

        List<Repository> actual = githubService.getNotForkRepositories("user");
        assertEquals(actual.get(0).getName(), expected.get(0).getName());
        assertEquals(actual.get(0).getOwnerLogin(), expected.get(0).getOwnerLogin());
        assertEquals(actual.get(0).getBranches().size(), expected.get(0).getBranches().size());
    }

    @Test
    public void getOnlyNotForkRepositories_failure_throwsException() {
        Mockito.when(githubClient.getUserRepositories(("user"))).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> githubService.getNotForkRepositories("user"));
    }
}

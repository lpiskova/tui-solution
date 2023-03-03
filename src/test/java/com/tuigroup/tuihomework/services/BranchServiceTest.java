package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.dto.BranchDto;
import com.tuigroup.tuihomework.client.model.Branch;
import com.tuigroup.tuihomework.client.model.Commit;
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
public class BranchServiceTest {

    @InjectMocks
    private GithubBranchService branchService;

    @Mock
    private GithubClient githubClient;

    private final String user = "user";

    private final String repository = "repository";

    @Test
    public void getUserRepositories_success() {
        List<Branch> branches = List.of(
                new Branch("branch1", new Commit("sha1")),
                new Branch("branch2", new Commit("sha2")),
                new Branch("branch3", new Commit("sha3")));

        Mockito.when(githubClient.getUserRepositoryBranches(user, repository)).thenReturn(branches);
        assertEquals(3, branchService.getBranches(user, repository).size());
    }

    @Test
    public void getUserRepositories_successWithNoRepositories() {
        Mockito.when(githubClient.getUserRepositoryBranches(user, repository)).thenReturn(List.of());
        assertEquals(0, branchService.getBranches(user, repository).size());
    }

    @Test
    public void getUserRepositoryBranches_success_compareResults() {
        String branchName = "branch";
        String commitSha = "sha";
        List<Branch> branches = List.of(new Branch(branchName, new Commit(commitSha)));

        Mockito.when(githubClient.getUserRepositoryBranches(user, repository)).thenReturn(branches);

        List<BranchDto> expected = List.of(new BranchDto(branchName, commitSha));
        List<Branch> actual = branchService.getBranches(user, repository);

        assertEquals(actual.get(0).getName(), expected.get(0).getName());
        assertEquals(actual.get(0).getCommit().getSha(), expected.get(0).getLastCommitSha());
    }

    @Test
    public void getUserRepositoryBranches_failure() {
        Mockito.when(githubClient.getUserRepositoryBranches(user, repository)).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> branchService.getBranches(user, repository));
    }
}

package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.model.Branch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubBranchService implements BranchService {

    private final GithubClient githubClient;

    @Override
    public List<Branch> getBranches(String user, String repository) {
        return githubClient.getUserRepositoryBranches(user, repository);
    }
}

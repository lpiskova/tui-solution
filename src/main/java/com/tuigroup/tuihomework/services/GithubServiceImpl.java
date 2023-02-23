package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.model.GithubRepository;
import com.tuigroup.tuihomework.view.Branch;
import com.tuigroup.tuihomework.view.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubServiceImpl implements GithubService {

    private final GithubClient githubClient;

    @Override
    public List<Repository> getNotForkRepositories(String user) {
        return githubClient.getUserRepositories(user)
                .stream()
                .filter(r -> !r.isFork())
                .map(r -> getRepository(user, r))
                .toList();
    }

    private Repository getRepository(String user, GithubRepository repository) {
        List<Branch> branches = githubClient
                .getUserRepositoryBranches(user, repository.getName())
                .stream()
                .map(b -> new Branch(b.getName(), b.getCommit().getSha()))
                .toList();
        return new Repository(repository.getName(), repository.getOwner().getLogin(), branches);
    }
}

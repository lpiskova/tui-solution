package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.model.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubRepositoryService implements RepositoryService {

    private final GithubClient githubClient;

    @Override
    public List<Repository> getNotForkRepositories(String user) {
        return getRepositories(user, Predicate.not(Repository::isFork));
    }

    @Override
    public List<Repository> getRepositories(String user, Predicate<Repository> predicate) {
        return githubClient.getUserRepositories(user)
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

}

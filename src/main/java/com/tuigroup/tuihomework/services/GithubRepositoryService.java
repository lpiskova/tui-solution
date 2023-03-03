package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.client.model.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubRepositoryService implements RepositoryService {

    private static final int FROM_PAGE = 1;

    @Value("${github.params.perPage}")
    private int perPage;

    private final GithubClient githubClient;

    @Override
    public List<Repository> getNotForkRepositories(String user) {
        return getRepositories(user, Predicate.not(Repository::isFork));
    }

    @Override
    public List<Repository> getRepositories(String user, Predicate<Repository> predicate) {
        return githubClient.getUserRepositories(user, FROM_PAGE, perPage)
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

}

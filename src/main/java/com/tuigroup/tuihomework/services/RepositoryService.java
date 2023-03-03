package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.model.Repository;

import java.util.List;
import java.util.function.Predicate;

public interface RepositoryService {

    List<Repository> getNotForkRepositories(String user);

    List<Repository> getRepositories(String user, Predicate<Repository> predicate);

}

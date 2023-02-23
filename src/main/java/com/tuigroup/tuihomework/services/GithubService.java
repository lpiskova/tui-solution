package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.view.Repository;

import java.util.List;

public interface GithubService {

    List<Repository> getNotForkRepositories(String user);

}

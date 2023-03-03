package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.client.model.Branch;

import java.util.List;

public interface BranchService {

    List<Branch> getBranches(String user, String repository);

}

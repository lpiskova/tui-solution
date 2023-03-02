package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.model.Branch;

import java.util.List;

public interface BranchService {

    List<Branch> getBranches(String user, String repository);

}

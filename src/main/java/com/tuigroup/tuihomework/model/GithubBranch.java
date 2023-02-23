package com.tuigroup.tuihomework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GithubBranch {

    private String name;

    private GithubCommit commit;

}
package com.tuigroup.tuihomework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GithubRepository {

    private String name;

    private GithubOwner owner;

    private boolean fork;

}

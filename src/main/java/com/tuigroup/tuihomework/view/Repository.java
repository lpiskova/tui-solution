package com.tuigroup.tuihomework.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Repository {

    private String name;

    private String ownerLogin;

    private List<Branch> branches;

}

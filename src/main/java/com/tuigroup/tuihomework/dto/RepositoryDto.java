package com.tuigroup.tuihomework.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RepositoryDto {

    private String name;

    private String ownerLogin;

    private List<BranchDto> branches;

}

package com.tuigroup.tuihomework.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BranchDto {

    private String name;

    private String lastCommitSha;

}

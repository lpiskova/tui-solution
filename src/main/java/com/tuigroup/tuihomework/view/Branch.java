package com.tuigroup.tuihomework.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Branch {

    private String name;

    private String lastCommitSha;

}

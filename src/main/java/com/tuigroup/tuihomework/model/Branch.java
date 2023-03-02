package com.tuigroup.tuihomework.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Branch {

    private String name;

    private Commit commit;

}

package com.tuigroup.tuihomework.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Repository {

    private String name;

    private Owner owner;

    private boolean fork;

}

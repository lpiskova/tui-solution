package com.tuigroup.tuihomework.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ErrorMessage {

    private Integer status;

    private String message;

}

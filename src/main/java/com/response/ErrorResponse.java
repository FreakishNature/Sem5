package com.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    String error;

    public static ErrorResponse errorResponseWithExistingField(String field){
        return new ErrorResponse("There exists user with such " + field);
    }

    public static ErrorResponse errorResponseWithMissingField(String field){
        return new ErrorResponse("The \"" + field + "\" has been lost");
    }

    public static ErrorResponse errorResponseNoSuchUsername(String username){
        return new ErrorResponse("There is no user with username: \"" + username + "\"");
    }

    public static ErrorResponse projectDoesNotExists(String projectName){
        return new ErrorResponse("The project " + projectName + " was not found.");

    }
}

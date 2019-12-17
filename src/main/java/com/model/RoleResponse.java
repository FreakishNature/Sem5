package com.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    int role;

    public static class Roles{
        public static int ADMIN = 3;
        public static int MEMBER = 2;
        public static int VIEWER = 1;
    }
}

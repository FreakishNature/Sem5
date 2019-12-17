package com.security;

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

public class Authorities {
    public static String ADMIN;
    public static String PROJECT_OWNER;
    public static String MODERATOR;
    public static String INVESTOR;
    public static String PROJECT_MEMBER;

    public static List<String> getListOfAuthorities(){
        return Arrays.asList(ADMIN,PROJECT_MEMBER,PROJECT_OWNER,MODERATOR,INVESTOR);
    }
}

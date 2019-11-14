package com.model;

import java.util.Arrays;
import java.util.List;

public class Status {
    public static String REJECTED = "REJECTED";
    public static String ACCEPTED = "ACCEPTED";
    public static String PENDING = "PENDING";

    public static List<String> ALLOWED_STATUSES = Arrays.asList(REJECTED, ACCEPTED, PENDING);
}

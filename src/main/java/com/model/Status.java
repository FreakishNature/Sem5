package com.model;

import java.util.Arrays;
import java.util.List;

public class Status {
    public static final String REJECTED = "REJECTED";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String PENDING = "PENDING";

    public static List<String> ALLOWED_STATUSES = Arrays.asList(REJECTED, ACCEPTED, PENDING);
}

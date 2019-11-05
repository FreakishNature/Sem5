package com.security;

public class JwtProperties {
    public static final String SECRET = "Secret";
    public static final int EXPIRATION_TIME = 60 * 60 * 2;
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";
}

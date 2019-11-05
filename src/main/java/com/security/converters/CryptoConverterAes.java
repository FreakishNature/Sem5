package com.security.converters;

import org.springframework.beans.factory.annotation.Value;

import javax.persistence.AttributeConverter;

public class CryptoConverterAes implements AttributeConverter<String,String> {
    @Value("${cryptoKey}")
    String key;
    @Value("${cryptoInitVector}")
    String initVector;

    @Override
    public String convertToDatabaseColumn(String s) {
        return Crypto.encrypt(key,initVector,s);
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return Crypto.decrypt(key,initVector,s);
    }
}



package com.security.converters;

import javax.persistence.AttributeConverter;

public class CryptoHash implements AttributeConverter<String,String> {
    @Override
    public String convertToDatabaseColumn(String s) {
        return null;
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return null;
    }
}

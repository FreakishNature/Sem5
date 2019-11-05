package com.security.converters;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.security.*;

public class CryptoConverterShaRsa implements AttributeConverter<String,String> {
    KeyPair pair;

    {
        try {
            pair = CryptoShaRsa.getKeyPairFromKeyStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String convertToDatabaseColumn(String s) {
        try {
            return CryptoShaRsa.encrypt(s, pair.getPublic());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String convertToEntityAttribute(String s) {
        try {
            return CryptoShaRsa.decrypt(s, pair.getPrivate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

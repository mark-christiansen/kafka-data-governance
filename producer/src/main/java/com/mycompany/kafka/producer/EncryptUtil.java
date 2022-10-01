package com.mycompany.kafka.producer;

import com.mycompany.kafka.governance.data.protection.rules.EntropyRule;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptUtil {
    public static byte[] encrypt(String clearText) throws Exception {

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        return cipher.doFinal(clearText.getBytes());
    }

    public static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}

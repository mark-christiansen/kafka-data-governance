package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.jupiter.api.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Entropy rule tests")
public class EntropyRuleTest {

/*
    private static SchemaLoader schemaLoader;
    private GenericRecord record;

    @BeforeAll
    public static void suiteSetup() throws IOException {
        schemaLoader = new SchemaLoader();
    }

    @BeforeEach
    public void testSetup() {

        Schema nestedSchema = schemaLoader.getSchema("nested");
        GenericRecord nested = new GenericRecordBuilder(nestedSchema)
                .set("long", 1L)
                .set("string", "required")
                .set("bytes", "required".getBytes())
                .set("stringArray", new String[] {"required"})
                .set("bytesArray", new byte[][] {"required".getBytes()})
                .build();

        Schema mainSchema = schemaLoader.getSchema("main");
        record = new GenericRecordBuilder(mainSchema)
                .set("long", 1L)
                .set("string", "required")
                .set("bytes", "required".getBytes())
                .set("stringArray", new String[] {"required"})
                .set("bytesArray", new byte[][] {"required".getBytes()})
                .set("record", nested)
                .build();
    }

    @AfterEach
    public void testCleanup() {
    }

    @Test
    @DisplayName("Test plaintext string")
    public void testPlainTextString() {
        EntropyRule rule = new EntropyRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "James Johnson");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test AES encrypted string")
    public void testEncryptedString() throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {

        String str = "James Johnson";

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(str.getBytes());
        //Base64.getEncoder().encodeToString(cipherText);

        EntropyRule rule = new EntropyRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", cipherText);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test Base64 encoded string")
    public void testBse64EncodedString() {
        EntropyRule rule = new EntropyRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", Base64.getEncoder().encode("James Johnson".getBytes()));
        assertFalse(rule.test(record));
    }
*/
}

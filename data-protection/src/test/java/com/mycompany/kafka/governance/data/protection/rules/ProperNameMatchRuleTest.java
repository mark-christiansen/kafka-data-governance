package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Proper name matching rule tests")
public class ProperNameMatchRuleTest {

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
    @DisplayName("Test matches English first name string")
    public void testMatchesEnglishFirstNameString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "Terry");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test matches English last name string")
    public void testMatchesEnglishLastNameString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "Porter");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test matches English full name string")
    public void testMatchesEnglishFullNameString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "Michael Jordan");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match English name in uppercase")
    public void testNotMatchesUppercaseNameString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "JUSTIN JEFFERSON");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match English name in lowercase")
    public void testNotMatchesLowercaseNameString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "tom jones");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match string")
    public void testNotMatchesFirstNameString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "Abcdefg");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match null string")
    public void testNotMatchesNullString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", null);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match empty string")
    public void testNotMatchesEmptyString() {
        ProperNameMatchRule rule = new ProperNameMatchRule("com.mycompany.kafka.model.test.Main", "string");
        record.put("string", "");
        assertFalse(rule.test(record));
    }
}

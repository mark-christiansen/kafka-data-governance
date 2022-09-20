package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Regular expression pattern matching rule tests")
public class PatternMatchRuleTest {

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
    @DisplayName("Test does not match unknown class")
    public void testNotMatchesUnknownClass() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Unknown", "string", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("string", "999-99-9999");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match unknown field")
    public void testNotMatchesUnknownField() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "unknown", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("string", "999-99-9999");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match non-string or non-bytes field")
    public void testNotMatchesLong() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "long", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("long", 1L);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nullable non-string or non-bytes field")
    public void testNotMatchesNullableLong() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableLong", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableLong", 1L);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches string")
    public void testMatchesString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "string", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("string", "999-99-9999");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match string")
    public void testNotMatchesString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "string", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("string", "abc1234");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nullable string")
    public void testMatchesNullableString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableString", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableString", "999-99-9999");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nullable string")
    public void testNotMatchesNullableString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableString", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableString", "abc1234");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nullable string that is null")
    public void testNotMatchesNullableStringWithNull() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableString", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableString", null);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches string array")
    public void testMatchesStringArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("stringArray", new String[] {"999-99-9999"});
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test matches string array with multiple values")
    public void testMatchesStringArrayWithMultipleValues() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("stringArray", new String[] {"acb123", "999-99-9999"});
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match string array")
    public void testNotMatchesStringArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("stringArray", new String[] {"abc1234"});
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match empty string array")
    public void testNotMatchesEmptyStringArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("stringArray", new String[] {});
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches bytes")
    public void testMatchesBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "bytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("bytes", "999-99-9999".getBytes());
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match bytes")
    public void testNotMatchesBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "bytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("bytes", "abc1234".getBytes());
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nullable bytes")
    public void testMatchesNullableBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableBytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableBytes", "999-99-9999".getBytes());
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nullable bytes")
    public void testNotMatchesNullableBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableBytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableBytes", "abc1234".getBytes());
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nullable bytes that is null")
    public void testNotMatchesNullableBytesWithNull() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableBytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("nullableBytes", null);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches bytes array")
    public void testMatchesBytesArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("bytesArray", new byte[][] { "999-99-9999".getBytes() });
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test matches bytes array with multiple values")
    public void testMatchesBytesArrayWithMultipleValues() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("bytesArray", new byte[][] { "abc123".getBytes(), "999-99-9999".getBytes() });
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match bytes array")
    public void testNotMatchesBytesArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("bytesArray", new byte[][] { "abc1234".getBytes() });
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match empty bytes array")
    public void testNotMatchesEmptyBytesArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        record.put("bytesArray", new byte[][] {});
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested non-string or non-bytes field")
    public void testNotMatchesNestedLong() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.long", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("long", 1L);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested nullable non-string or non-bytes field")
    public void testNotMatchesNestedNullableLong() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableLong", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableLong", 1L);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested string")
    public void testMatchesNestedString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.string", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("string", "999-99-9999");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested string")
    public void testNotMatchesNestedString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.string", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("string", "abc1234");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested nullable string")
    public void testMatchesNestedNullableString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableString", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableString", "999-99-9999");
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested nullable string")
    public void testNotMatchesNestedNullableString() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableString", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableString", "abc1234");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested nullable string that is null")
    public void testNotMatchesNestedNullableStringWithNull() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableString", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableString", null);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested string array")
    public void testMatchesNestedStringArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("stringArray", new String[] {"999-99-9999"});
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested string array with multiple values")
    public void testMatchesNestedStringArrayWithMultipleValues() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("stringArray", new String[] {"999-99-9999", "abc123"});
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested string array")
    public void testNotMatchesNestedStringArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("stringArray", new String[] {"abc1234"});
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested empty string array")
    public void testNotMatchesNestedEmptyStringArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("stringArray", new String[] {});
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested bytes")
    public void testMatchesNestedBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.bytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("bytes", "999-99-9999".getBytes());
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested bytes")
    public void testNotMatchesNestedBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.bytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("bytes", "abc1234".getBytes());
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested nullable bytes")
    public void testMatchesNestedNullableBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableBytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableBytes", "999-99-9999".getBytes());
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested nullable bytes")
    public void testNotMatchesNestedNullableBytes() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableBytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableBytes", "abc1234".getBytes());
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested nullable bytes that is null")
    public void testNotMatchesNestedNullableBytesWithNull() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.nullableBytes", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("nullableBytes", null);
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested bytes array")
    public void testMatchesNestedBytesArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("bytesArray", new byte[][] { "999-99-9999".getBytes() });
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test matches nested bytes array with multiple values")
    public void testMatchesNestedBytesArrayWithMultipleValues() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("bytesArray", new byte[][] { "999-99-9999".getBytes(), "abc123".getBytes() });
        assertTrue(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested bytes array")
    public void testNotMatchesNestedBytesArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("bytesArray", new byte[][] { "abc1234".getBytes() });
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested empty bytes array")
    public void testNotMatchesNestedEmptyBytesArray() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "record.bytesArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        ((GenericRecord) record.get("record")).put("bytesArray", new byte[][] {});
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested string array on null record")
    public void testNotMatchesNestedStringArrayOnNullRecord() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "nullableRecord.stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        assertFalse(rule.test(record));
    }

    @Test
    @DisplayName("Test does not match nested string array from non-record field")
    public void testNotMatchesNestedStringArrayFromNonRecordField() {
        PatternMatchRule rule = new PatternMatchRule("com.mycompany.kafka.model.test.Main", "string.stringArray", "^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        assertFalse(rule.test(record));
    }
}

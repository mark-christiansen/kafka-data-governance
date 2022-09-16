package com.mycompany.kafka.governance.interceptors.util;

import org.apache.avro.util.Utf8;

public class AvroUtil {

    public static String[] getStringBasedValues(Object fieldValue) {

        if (fieldValue instanceof String[]) {
            return (String[]) fieldValue;
        } else if (fieldValue instanceof String) {
            return new String[] {(String) fieldValue};
        } else if (fieldValue instanceof Utf8) {
            return new String[] { new String(((Utf8) fieldValue).getBytes()) };
        } else if (fieldValue instanceof Utf8[]) {

            Utf8[] utf8Array = (Utf8[]) fieldValue;
            String[] stringArray = new String[utf8Array.length];
            for (int j = 0; j < utf8Array.length; j++) {
                stringArray[j] = new String(utf8Array[j].getBytes());
            }
            return stringArray;

        } else if (fieldValue instanceof byte[][]) {

            byte[][] bytesArray = (byte[][]) fieldValue;
            String[] stringArray = new String[bytesArray.length];
            for (int j = 0; j < bytesArray.length; j++) {
                stringArray[j] = new String(bytesArray[j]);
            }
            return stringArray;

        } else if (fieldValue instanceof byte[]) {
            return new String[] {new String((byte[]) fieldValue)};
        }
        return null;
    }
}

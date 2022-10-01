package com.mycompany.kafka.governance.data.protection.rules;

import com.mycompany.kafka.governance.data.protection.RuleViolation;
import com.mycompany.kafka.governance.data.protection.util.AvroUtil;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.String.format;

/**
 * @deprecated
 * The idea of this rule was to evaluate whether a string was encrypted based on how much its size changes when
 * compressed. The idea is that if a string changes size drastically when compressed then it isn't encrypted. This ended
 * up not being a good indicator, especially for smaller strings. This might be a good way to detect if a file is
 * encrypted or not.
 */
@Deprecated
public class CompressionRateRule extends FieldMatchingRuleImpl<String> {

    private static final Logger log  = LoggerFactory.getLogger(CompressionRateRule.class);
    private static final double MINIMUM_EXPECTED_COMPRESSION_RATE = 1.25;

    private final byte[] buffer = new byte[1024];

    public CompressionRateRule(String recordName, String fieldName) {
        super(recordName, fieldName);
    }

    @Override
    protected boolean test(String[] fieldValue) {

        for (String value : fieldValue) {
            try {
                if (calculateCompressionRate(value) > MINIMUM_EXPECTED_COMPRESSION_RATE) {
                    return true;
                }
            } catch (IOException e) {
                log.warn(format("Error calculating compression rate of string \"%s\"", value), e);
            }
        }

        return false;
    }

    @Override
    protected String[] getFieldValues(Object value) {
        return AvroUtil.getStringBasedValues(value);
    }

    @Override
    public RuleViolation getViolationMessage() {
        return new RuleViolation(getRecordName(), getFieldName(), "Value was recognized as not encrypted because of its low compression rate");
    }

    private double calculateCompressionRate(String value) throws IOException {

        byte[] bytes = value.getBytes();
        final int decompressedLength = bytes.length;

        LZ4Factory factory = LZ4Factory.fastestJavaInstance();
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(bytes, 0, decompressedLength, compressed, 0, maxCompressedLength);

        return (double) decompressedLength / (double) compressedLength;
    }
}

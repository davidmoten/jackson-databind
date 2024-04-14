package com.fasterxml.jackson.databind.deser.std;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NumericDeserializationReviewMain {

    public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
        String byteJson = String.valueOf(Byte.MAX_VALUE);
        String smallDecimalFormInteger = "100.0";
        String smallExponentialFormInteger = "1e2";
        String shortJson = String.valueOf(Short.MAX_VALUE);
        String intJson = String.valueOf(Integer.MAX_VALUE);
        String longJson = String.valueOf(Long.MAX_VALUE);
        String bigIntegerJson = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString();
        String bigIntegerExponentialFormJson = "1.23e56";
        String floatJson = String.valueOf(Float.MAX_VALUE);
        String doubleJson = String.valueOf(Double.MAX_VALUE);
        String bigDecimalJson = BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN).add(new BigDecimal("1.2"))
                .toString();

        List<String> values = Arrays.asList(byteJson, smallDecimalFormInteger, smallExponentialFormInteger, shortJson,
                intJson, longJson, bigIntegerJson, bigIntegerExponentialFormJson, floatJson, doubleJson,
                bigDecimalJson);
        List<Class<?>> classes = Arrays.asList(ByteProperty.class, ShortProperty.class, IntProperty.class,
                LongProperty.class, FloatProperty.class, DoubleProperty.class, BigIntegerProperty.class,
                BigDecimalProperty.class);

        for (String value : values) {
            System.out.println("------ " + value + "-----------");
            for (Class<?> cls : classes) {
                String result;
                String v;
                try {
                    v = new ObjectMapper().readValue(json(value), cls).toString();
                    if (v.endsWith(".0")) {
                        v = v.substring(0, v.length() - 2);
                    }
                    if (v.equals(value)) {
                        result = "Ok";
                    } else {
                        result = "?";
                    }
                } catch (Throwable e) {
                    v = value;
                    result = "Err";
                }
                System.out.println(result + " " + cls.getSimpleName() + " " + v);
            }
        }
    }

    // Conclusions for property deserialization
    //
    // integer types give error on overflow (good)
    // integer in exponential form 1e2 not parsed by any integer type (fail)
    // integer with decimal point (100.0) not parsed by any integer type (fail)
    // decimal types go to Infinity on overflow (acceptable)
    // decimal types lose precision rather than throw (good)

    private static String json(String value) {
        return "{\"name\":\"saturn\",\"value\":\"" + value + "\"}";
    }

    public static final class ByteProperty {
        final String name;
        final byte value;

        public ByteProperty(@JsonProperty("name") String name, @JsonProperty("value") byte value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class ShortProperty {
        final String name;
        final short value;

        public ShortProperty(@JsonProperty("name") String name, @JsonProperty("value") short value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class IntProperty {
        final String name;
        final int value;

        public IntProperty(@JsonProperty("name") String name, @JsonProperty("value") int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class LongProperty {
        final String name;
        final long value;

        public LongProperty(@JsonProperty("name") String name, @JsonProperty("value") long value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class FloatProperty {
        final String name;
        final float value;

        public FloatProperty(@JsonProperty("name") String name, @JsonProperty("value") float value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class DoubleProperty {
        final String name;
        final double value;

        public DoubleProperty(@JsonProperty("name") String name, @JsonProperty("value") double value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class BigIntegerProperty {
        final String name;
        final BigInteger value;

        public BigIntegerProperty(@JsonProperty("name") String name, @JsonProperty("value") BigInteger value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static final class BigDecimalProperty {
        final String name;
        final BigDecimal value;

        public BigDecimalProperty(@JsonProperty("name") String name, @JsonProperty("value") BigDecimal value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

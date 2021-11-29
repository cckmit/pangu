package com.pangu.framework.utils.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.pangu.framework.utils.codec.CryptUtils;
import com.pangu.framework.utils.lang.NumberUtils;
import com.pangu.framework.utils.time.DateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * JSON 转换相关的工具类 注意,Map的Key只能为简单类型 ,不可采用复杂类型.
 *
 * @author author
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class JsonUtils {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();
    private static final long LONG_JS_MAX_VLAUE = 1L << 53;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d*$");
    private static final Pattern DATE_1_PATTERN = Pattern.compile("^19\\d{12}$");
    private static final Pattern DATE_2_PATTERN = Pattern.compile("^20\\d{12}$");
    private static final Pattern DATE_3_PATTERN = Pattern.compile("^\\d{4}[-]\\d{2}$");
    private static final Pattern DATE_4_PATTERN = Pattern.compile("^\\d{4}[-]\\d{2}[-]\\d{2}$");
    private static final Pattern DATE_5_PATTERN = Pattern
            .compile("^\\d{4}[-]\\d{2}[-]\\d{2} \\d{2}[:]\\d{2}[:]\\d{2}$");
    private static final Pattern DATE_6_PATTERN = Pattern
            .compile("^\\d{4}[-]\\d{2}[-]\\d{2} \\d{2}[:]\\d{2}[:]\\d{2}[.]\\d{3}$");

    private static final ObjectMapper MAPPER_CONVERT = new ObjectMapper();

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        MAPPER.enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        MAPPER.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Long
        SimpleModule module = new SimpleModule();
        JsonSerializer<Long> longSerializer = new JsonSerializer<Long>() {
            public void serialize(Long value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (value >= LONG_JS_MAX_VLAUE) {
                    jgen.writeString(value.toString());
                } else {
                    jgen.writeNumber(value);
                }
            }

        };
        JsonDeserializer<? extends Long> longDeserializer = new JsonDeserializer<Long>() {
            public Long deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                return Long.valueOf(jp.getValueAsString());
            }
        };
        // BIGINTEGER
        JsonSerializer<BigInteger> bigIntSerializer = new JsonSerializer<BigInteger>() {
            public void serialize(BigInteger value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                if (value.longValue() >= LONG_JS_MAX_VLAUE) {
                    jgen.writeString(value.toString());
                } else {
                    jgen.writeNumber(value);
                }
            }
        };
        // BIGDECIMAL
        JsonSerializer<BigDecimal> bigDecSerializer = new JsonSerializer<BigDecimal>() {
            public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                jgen.writeString(String.valueOf(value));
            }
        };
        // BITSET
        JsonSerializer<BitSet> bitsetSerializer = new JsonSerializer<BitSet>() {
            public void serialize(BitSet value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeString(CryptUtils.byte2hex(value.toByteArray()));
            }
        };
        JsonDeserializer<? extends BitSet> bitsetDeserializer = new JsonDeserializer<BitSet>() {
            public BitSet deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                return BitSet.valueOf(CryptUtils.hex2byte(jp.getValueAsString().getBytes()));
            }
        };
        // DATE
        JsonDeserializer<Date> dateDeserializer = new JsonDeserializer<Date>() {
            public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                String text = jp.getValueAsString();
                if (StringUtils.isEmpty(text)) {
                    return null;
                }
                if (NUMBER_PATTERN.matcher(text).matches()) {
                    if (DATE_2_PATTERN.matcher(text).matches() || DATE_1_PATTERN.matcher(text).matches()) {
                        return DateUtils.string2Date(text, "yyyyMMddHHmmss");
                    }
                    // MS
                    return new Date(Long.valueOf(text));
                }
                if (DATE_3_PATTERN.matcher(text).matches()) {
                    return DateUtils.string2Date(text, "yyyy-MM");
                }
                if (DATE_4_PATTERN.matcher(text).matches()) {
                    return DateUtils.string2Date(text, "yyyy-MM-dd");
                }
                if (DATE_5_PATTERN.matcher(text).matches()) {
                    return DateUtils.string2Date(text, "yyyy-MM-dd HH:mm:ss");
                }
                if (DATE_6_PATTERN.matcher(text).matches()) {
                    return DateUtils.string2Date(text, "yyyy-MM-dd HH:mm:ss.SSS");
                }
                throw new RuntimeException("日期数据格式不符 - " + text);
            }
        };
        module.addSerializer(long.class, longSerializer);
        module.addSerializer(Long.class, longSerializer);
        module.addSerializer(BigInteger.class, bigIntSerializer);
        module.addSerializer(BigDecimal.class, bigDecSerializer);
        module.addSerializer(BitSet.class, bitsetSerializer);

        module.addDeserializer(long.class, longDeserializer);
        module.addDeserializer(Long.class, longDeserializer);
        module.addDeserializer(BitSet.class, bitsetDeserializer);
        module.addDeserializer(Date.class, dateDeserializer);

        // COLLECTION
        module.addSerializer(Collection.class, new JsonSerializer<Collection>() {
            public void serialize(Collection value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                jgen.writeStartArray();
                Iterator it = value.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    jgen.writeObject(o);
                }
                jgen.writeEndArray();
            }
        });
        // MAP
        module.addSerializer(Map.class, new JsonSerializer<Map>() {
            public void serialize(Map value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                Iterator<Entry> it = value.entrySet().iterator();
                while (it.hasNext()) {
                    Entry o = it.next();
                    Object k = o.getKey();
                    Object v = o.getValue();
                    String fn;
                    if (k instanceof String) {
                        fn = (String) k;
                    } else if (k instanceof Enum) {
                        fn = ((Enum) k).name();
                    } else if (k instanceof Number) {
                        fn = k.toString();
                    } else {
                        StringWriter out = new StringWriter();
                        MAPPER.writeValue(out, k);
                        fn = out.toString();
                        int len = fn.length();
                        if (fn.charAt(0) == '\"' && fn.charAt(len - 1) == '\"') {
                            fn = fn.substring(1, len - 1);
                        }
                    }
                    jgen.writeObjectField(fn, v);
                }
                jgen.writeEndObject();
            }
        });

        MAPPER.registerModule(module);
    }

    private JsonUtils() {
        throw new IllegalAccessError("该类不允许实例化");
    }

    /**
     * 将对象转换为 JSON 的字符串格式
     *
     * @param obj 被转换的对象
     * @return 当参数为空时会返回null
     */
    public static String object2String(Object obj) {
        if (obj == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        try {
            MAPPER.writeValue(writer, obj);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将对象[{}]转换为JSON字符串时发生异常", obj, e);
            throw new RuntimeException(message.getMessage(), e);
        }
        return writer.toString();
    }

    public static byte[] object2Bytes(Object obj) {
        if (obj == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        try {
            return MAPPER.writeValueAsBytes(obj);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将对象[{}]转换为JSON字符串时发生异常", obj, e);
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将 JSON 格式的字符串转换为 map
     *
     * @param json JSON，允许为空
     * @return json为null时会返回空的Map实例
     */
    public static Map<String, Object> string2Map(String json) {
        try {
            if (StringUtils.isBlank(json)) {
                return HashMap.class.newInstance();
            }
            JavaType type = TYPE_FACTORY.constructMapType(HashMap.class, String.class, Object.class);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为Map时出现异常", json);
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将 JSON 格式的字符串转换为数组
     *
     * @param <T>
     * @param json 字符串
     * @param clz  数组类型
     * @return json为null时会返回null
     */
    public static <T> T[] string2Array(String json, Class<T> clz) {
        try {
            if (StringUtils.isBlank(json)) {
                return null;
            }
            JavaType type = TYPE_FACTORY.constructArrayType(clz);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为数组时出现异常", json, e);
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将 JSON 格式的字符串转换为对象
     *
     * @param <T>
     * @param json 字符串
     * @param clz  对象类型
     * @return json为null时会返回null
     */
    public static <T> T string2Object(String json, Class<T> clz) {
        try {
            if (StringUtils.isBlank(json) || clz == null) {
                return null;
            }
            if (clz.isEnum()) {
                return MAPPER.convertValue(json, clz);
            }
            JavaType type = TYPE_FACTORY.constructType(clz);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为对象[{}]时出现异常",
                    new Object[]{json, clz.getSimpleName(), e});
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将 JSON 格式的字符串转换为对象
     *
     * @param <T>
     * @param json 字符串
     * @param type 对象类型
     * @return json为null时会返回null
     */
    public static <T> T string2Object(String json, Type type) {
        try {
            if (StringUtils.isBlank(json) || type == null) {
                return null;
            }
            if (type instanceof Class && ((Class<T>) type).isEnum()) {
                return MAPPER.convertValue(json, (Class<T>) type);
            }
            JavaType t = TYPE_FACTORY.constructType(type);
            return MAPPER.readValue(json, t);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为对象[{}]时出现异常", new Object[]{json, type, e});
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /***
     * json 泛型转换
     * @param tr 示例 new TypeReference<List<Long>>(){}
     **/
    public static <T> T string2GenericObject(String json, TypeReference<T> tr) {
        if (StringUtils.isBlank(json)) {
            return null;
        } else {
            try {
                return MAPPER.readValue(json, tr);
            } catch (Exception e) {
                FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为[{}]时出现异常", new Object[]{json, tr});
                throw new RuntimeException(message.getMessage(), e);
            }
        }
    }

    /**
     * 将 JSON 格式的字符串转换为对象
     *
     * @param <T>
     * @param json 字符串
     * @param type 对象类型
     * @return json为null时会返回null
     */
    public static <T> T bytes2Object(byte[] json, Type type) {
        try {
            if (json == null || json.length == 0) {
                return null;
            }
            JavaType t = TYPE_FACTORY.constructType(type);
            return MAPPER.readValue(json, t);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为对象[{}]时出现异常", new Object[]{json, type, e});
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /***
     * json数组泛型转换
     * @param tr 示例 new TypeReference<List<Long>>(){}
     **/
    public static <T> T bytes2GenericObject(byte[] json, TypeReference<T> tr) {
        if (json == null || json.length == 0) {
            return null;
        } else {
            try {
                return MAPPER.readValue(json, tr);
            } catch (Exception e) {
                FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为[{}]时出现异常", new Object[]{json, tr});
                throw new RuntimeException(message.getMessage(), e);
            }
        }
    }

    /**
     * 将 JSON 格式的字符串转换为集合
     *
     * @param <T>
     * @param json           字符串
     * @param collectionType 集合类型
     * @param elementType    元素类型
     * @return json为null时会返回空的集合实例
     */
    public static <C extends Collection<E>, E> C string2Collection(String json, Class<C> collectionType,
                                                                   Class<E> elementType) {
        try {
            if (StringUtils.isBlank(json)) {
                return collectionType.newInstance();
            }
            JavaType type = TYPE_FACTORY.constructCollectionType(collectionType, elementType);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为集合[{}]时出现异常", new Object[]{json,
                    collectionType.getSimpleName(), e});
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将字符串转换为{@link HashMap}对象实例
     *
     * @param json      被转换的字符串
     * @param keyType   键类型
     * @param valueType 值类型
     * @return json为null时会返回空的HashMap实例
     */
    public static <K, V> Map<K, V> string2Map(String json, Class<K> keyType, Class<V> valueType) {
        try {
            if (StringUtils.isBlank(json)) {
                return HashMap.class.newInstance();
            }
            JavaType type = TYPE_FACTORY.constructMapType(HashMap.class, keyType, valueType);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为Map时出现异常", json);
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将字符串转换为特定的{@link Map}对象实例
     *
     * @param json      被转换的字符串
     * @param keyType   键类型
     * @param valueType 值类型
     * @param mapType   指定的{@link Map}类型
     * @return json为空时会返回空的Map实例
     */
    public static <M extends Map<K, V>, K, V> M string2Map(String json, Class<K> keyType, Class<V> valueType,
                                                           Class<M> mapType) {
        try {
            if (StringUtils.isBlank(json)) {
                return mapType.newInstance();
            }
            JavaType type = TYPE_FACTORY.constructMapType(mapType, keyType, valueType);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为Map时出现异常", json);
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将字符串转换为特定的{@link Map}对象实例
     *
     * @param json      被转换的字符串
     * @param keyType   键类型
     * @param valueType 值类型
     * @param mapType   指定的{@link Map}类型
     * @return json为空时会返回空的Map实例
     */
    public static <M extends Map<K, V>, K, V> M string2Map(String json, Type keyType, Type valueType,
                                                           Class<M> mapType) {
        try {
            if (StringUtils.isBlank(json)) {
                return mapType.newInstance();
            }
            JavaType type = TYPE_FACTORY.constructMapType(mapType, TYPE_FACTORY.constructType(keyType), TYPE_FACTORY.constructType(valueType));
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将字符串[{}]转换为Map时出现异常", json);
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将 JSON 对象类型转换
     *
     * @param <T>
     * @param value 字符串
     * @param type  对象类型
     * @return json为null时会返回null
     */
    public static <T> T convertObject(Object value, Type type) {
        try {
            if (value == null) {
                return (T) value;
            }
            if (TypeUtils.isAssignable(type, Number.class) && value instanceof Number) {
                return NumberUtils.valueOf(type, (Number) value);
            }
            if (!(value instanceof Collection && TypeUtils.isAssignable(type, Collection.class))
                    && !(value instanceof Map && TypeUtils.isAssignable(type, Map.class))) {
                // XXX 实例泛型数据丢失，无法判断
                if (TypeUtils.isInstance(value, type)) {
                    return (T) value;
                }
            }
            JavaType t = TYPE_FACTORY.constructType(type);
            return MAPPER_CONVERT.convertValue(value, t);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将对象[{}]转换为类型[{}]时出现异常", new Object[]{value, type, e});
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * 将 JSON 对象类型转换
     *
     * @param <T>
     * @param value 字符串
     * @param type  对象类型
     * @return json为null时会返回null
     */
    public static <T> T convertObject(Object value, TypeReference<T> tr) {
        Type type = tr.getType();
        return convertObject(value, type);
    }

    /**
     * java map 转换对象
     *
     * @param mapData 原始数据
     * @param tr      转换类型
     */
    public static <T> T map2Object(Map mapData, TypeReference<T> tr) {
        if (mapData == null) {
            return (T) mapData;
        }
        try {
            if (TypeUtils.isInstance(mapData, tr.getType())) {
                return (T) mapData;
            }
            return MAPPER_CONVERT.convertValue(mapData, tr);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("将MAP[{}]转换为[{}]时出现异常", new Object[]{mapData, tr});
            throw new RuntimeException(message.getMessage(), e);
        }
    }

    /**
     * java list 转换对象
     *
     * @param values 原始数据
     * @param keys   对应 values 索引KEY
     * @param tr     转换类型
     */
    public static <T> T list2Object(List<?> values, String[] keys, TypeReference<T> tr) {
        Map<String, Object> mapData = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length && i < values.size(); i++) {
            Object value = values.get(i);
            if (value == null) {
                continue;
            }
            String key = keys[i];

            mapData.put(key, value);
        }
        return map2Object(mapData, tr);
    }

}

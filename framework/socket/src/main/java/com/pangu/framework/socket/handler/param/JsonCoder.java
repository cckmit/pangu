package com.pangu.framework.socket.handler.param;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.type.*;
import com.pangu.framework.socket.core.CoderType;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class JsonCoder implements Coder {

    static final ObjectMapper MAPPER;
    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    private static final long LONG_JS_MAX_VALUE = 1L << 53;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        MAPPER.enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        MAPPER.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Long
        SimpleModule module = new SimpleModule();
        JsonSerializer<Long> longSerializer = new JsonSerializer<Long>() {
            public void serialize(Long value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                if (Math.abs(value) >= LONG_JS_MAX_VALUE) {
                    jgen.writeString(value.toString());
                } else {
                    jgen.writeNumber(value);
                }
            }

        };
        JsonDeserializer<? extends Long> longDeserializer = new JsonDeserializer<Long>() {
            public Long deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException {
                return Long.valueOf(jp.getValueAsString());
            }
        };

        module.addSerializer(long.class, longSerializer);
        module.addSerializer(Long.class, longSerializer);
        module.addDeserializer(long.class, longDeserializer);
        module.addDeserializer(Long.class, longDeserializer);
        MAPPER.registerModule(module);
    }

    @Override
    public byte getFormat() {
        return CoderType.JSON;
    }

    @Override
    public Object[] decodeRequest(Message message, Session session, Parameters params, CompletableFuture<?> completableFuture, Attachment attachment) {
        Parameter[] parameters = params.getParameters();
        if (parameters == null || parameters.length == 0) {
            return new Object[0];
        }
        List<Object> objects = new ArrayList<>(parameters.length);

        JsonNode jsonNode = null;
        if (params.isInBody()) {
            try {
                jsonNode = MAPPER.readTree(message.getBody());
            } catch (IOException e) {
                log.error("反序列化失败[{}]", new String(message.getBody()), e);
                throw new SocketException(ExceptionCode.DECODE_ERROR, e);
            }
        }

        for (Parameter parameter : parameters) {
            if (parameter instanceof InBodyParameter) {
                InBodyParameter real = (InBodyParameter) parameter;
                Object value = parseInBodyParam(real, jsonNode, message);
                objects.add(value);
                continue;
            }
            if (parameter instanceof AttachmentIdParameter) {
                objects.add(attachment.getIdentity());
                continue;
            }
            if (parameter instanceof IdentityParameter) {
                objects.add(session.getIdentity());
                continue;
            }
            if (parameter instanceof MessageParameter) {
                objects.add(message);
                continue;
            }
            if (parameter instanceof SessionParameter) {
                objects.add(session);
                continue;
            }
            if (parameter instanceof FutureParameter) {
                objects.add(completableFuture);
                continue;
            }
            if (parameter instanceof BodyParameter) {
                BodyParameter bodyParameter = (BodyParameter) parameter;
                Type type = bodyParameter.getType();
                if (type == byte[].class && bodyParameter.isRaw()) {
                    objects.add(message.getBody());
                    continue;
                }
                try {
                    Object o = MAPPER.readValue(message.getBody(), TYPE_FACTORY.constructType(type));
                    objects.add(o);
                    continue;
                } catch (IOException e) {
                    log.error("处理Body数据[{}]提取时出现异常", new String(message.getBody()), e);
                    throw new SocketException(ExceptionCode.PARAM_PHASE, e);
                }
            }
            if (parameter instanceof InSessionParameter) {
                InSessionParameter rel = (InSessionParameter) parameter;
                String key = rel.getKey();
                Object sessionCtx = session.getCtx(key);
                if (sessionCtx == null) {
                    if (rel.isRequired()) {
                        log.info("请求[{}]需要session数据[{}]不存在", message.getHeader().getCommand(), key);
                        throw new SocketException(ExceptionCode.SESSION_PARAM);
                    }
                }
                objects.add(sessionCtx);
            }
        }
        return objects.toArray(new Object[0]);
    }

    @Override
    public byte[] encodeParameters(Object[] args, Parameters params) {
        Parameter[] parameters = params.getParameters();
        int size = args == null ? 0 : args.length;
        if (size != parameters.length) {
            throw new IllegalArgumentException("请求参数数量[" + size + "不一致[" + parameters.length + "]");
        }
        if (size == 0) {
            return new byte[0];
        }
        // inBody和body只会同时出现一种
        if (params.isInBody()) {
            Map<String, Object> values = new HashMap<>(size);
            for (int i = 0; i < size; ++i) {
                Parameter parameter = parameters[i];
                if (!(parameter instanceof InBodyParameter)) {
                    continue;
                }
                values.put(((InBodyParameter) parameter).getName(), args[i]);
            }
            try {
                return MAPPER.writeValueAsBytes(values);
            } catch (JsonProcessingException e) {
                throw new SocketException(ExceptionCode.ENCODE_ERROR, e);
            }
        } else {
            for (int i = 0; i < size; ++i) {
                Parameter parameter = parameters[i];
                if (!(parameter instanceof BodyParameter)) {
                    continue;
                }
                BodyParameter bodyParameter = (BodyParameter) parameter;
                if (args[i] instanceof byte[] && bodyParameter.isRaw()) {
                    return (byte[]) args[i];
                }
                try {
                    return MAPPER.writeValueAsBytes(args[i]);
                } catch (JsonProcessingException e) {
                    throw new SocketException(ExceptionCode.ENCODE_ERROR, e);
                }
            }
        }
        return new byte[0];
    }

    @Override
    public byte[] encodeResponse(Object result) {
        if (result == null) {
            return new byte[0];
        }
        try {
            return MAPPER.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            log.warn("json编码返回值[{}]异常", result, e);
            throw new SocketException(ExceptionCode.ENCODE_ERROR, e);
        }
    }

    @Override
    public Object decodeResponse(Message message, MethodDefine methodDefine) {
        try {
            return MAPPER.readValue(message.getBody(), TYPE_FACTORY.constructType(methodDefine.getResponse()));
        } catch (IOException e) {
            log.error("反序列化失败[{}]", new String(message.getBody()), e);
            throw new SocketException(ExceptionCode.DECODE_ERROR, e);
        }
    }

    private Object parseInBodyParam(InBodyParameter real, JsonNode jsonNode, Message message) {
        String name = real.getName();
        if (jsonNode == null) {
            try {
                jsonNode = MAPPER.readTree(message.getBody());
            } catch (IOException e) {
                log.error("[{}]反序列化失败[{}]", message.getHeader(), new String(message.getBody()), e);
                throw new SocketException(ExceptionCode.DECODE_ERROR, e);
            }
        }
        JsonParser parser = null;
        if (jsonNode.has(name)) {
            parser = jsonNode.get(name).traverse();
        } else if (real.isRequired()) {
            log.warn("消息[{}]注释[{}]请求的参数不存在", message.getHeader().getCommand(), real.getName());
            throw new SocketException(ExceptionCode.PARAM_PHASE);
        }
        if (parser == null) {
            return null;
        }
        parser.setCodec(MAPPER);
        try {
            JavaType javaType = TYPE_FACTORY.constructType(real.getType());
            return parser.getCodec().readValue(parser, javaType);
        } catch (Exception e) {
            log.error("[{}]处理InBody数据[{}]提取时出现异常", message.getHeader(), new String(message.getBody()), e);
            throw new SocketException(ExceptionCode.PARAM_PHASE);
        }
    }
}

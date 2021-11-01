package com.pangu.framework.protocol;

import com.pangu.framework.protocol.def.EnumDef;
import com.pangu.framework.protocol.def.TypeDef;
import com.pangu.framework.protocol.proxy.*;
import com.pangu.framework.protocol.proxy.*;
import com.pangu.framework.utils.codec.CryptUtils;
import com.pangu.framework.utils.codec.ZlibUtils;
import com.pangu.framework.utils.json.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 编解码上下文
 *
 * @author Ramon
 */
public class Definition {
    protected Logger log = LoggerFactory.getLogger(getClass());

    // 枚举定义
    private final AtomicInteger enumCurrent = new AtomicInteger();
    private final ConcurrentMap<Integer, EnumDef> enumIdxs = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, EnumDef> enumDefs = new ConcurrentHashMap<>();
    // 类型定义
    private final AtomicInteger typeCurrent = new AtomicInteger();
    private final ConcurrentMap<Integer, TypeDef> typeIdxs = new ConcurrentHashMap<Integer, TypeDef>();
    private final ConcurrentMap<Type, TypeDef> typeDefs = new ConcurrentHashMap<>();
    // 类型映射
    private final ConcurrentMap<Type, TypeDef> aliasDefs = new ConcurrentHashMap<>();
    // MAP对象
    private final ConcurrentMap<Type, TypeDef> mappedDefs = new ConcurrentHashMap<>();
    // 代理类
    private final ConcurrentMap<Byte, Proxy<?>> proxys = new ConcurrentHashMap<>();

    private volatile byte[] description;
    private volatile String md5Description;

    public void register(Class<?> clz, int index) {
        if (log.isDebugEnabled()) {
            log.debug("注册传输对象类型 [{}]", clz);
        }
        if (clz.isEnum()) {
            if (enumDefs.get(clz) == null) {
                int code = (index > 0 ? index : enumCurrent.incrementAndGet());
                EnumDef def = EnumDef.valueOf(code, clz);
                enumIdxs.put(code, def);
                enumDefs.put(clz, def);
            }
        } else {
            if (typeDefs.get(clz) == null) {
                int code = (index > 0 ? index : typeCurrent.incrementAndGet());
                TypeDef def = TypeDef.valueOf(code, clz);
                typeIdxs.put(code, def);
                typeDefs.put(clz, def);
            }
        }
        description = null;
    }

    /**
     * 获取类型定义
     *
     * @param def
     * @return
     */
    TypeDef getTypeDef(int def) {
        return typeIdxs.get(def);
    }

    /**
     * 获取类型定义
     *
     * @param type
     * @return
     */
    TypeDef getTypeDef(final Class<?> type) {
        TypeDef typeDef = typeDefs.get(type);
        Class<?> superType = type;
        while (typeDef == null) {
            typeDef = aliasDefs.get(superType);
            if (typeDef != null) {
                // 存在类型映射
                break;
            }
            superType = superType.getSuperclass();
            if (superType == Object.class) {
                typeDef = TypeDef.NULL;
                aliasDefs.put(type, typeDef);
                break;
            }
            typeDef = typeDefs.get(superType);
            if (typeDef != null) {
                aliasDefs.put(type, typeDef);
                break;
            }
        }
        return typeDef;
    }

    /**
     * 获取枚举定义
     *
     * @param def
     * @return
     */
    EnumDef getEnumDef(int def) {
        return enumIdxs.get(def);
    }

    /**
     * 获取枚举定义
     *
     * @param def
     * @return
     */
    EnumDef getEnumDef(Class<?> def) {
        return enumDefs.get(def);
    }

    // TypeDef getMappedDef(int def) {
    // return mappedIdxs.get(def);
    // }

    /**
     * 获取MAPPED类型定义
     *
     * @param type
     * @param createNew
     * @return
     */
    TypeDef getMappedDef(Class<?> type, boolean createNew) {
        TypeDef typeDef = mappedDefs.get(type);
        if (createNew && typeDef == null) {
            typeDef = TypeDef.valueOf(-1, type);
            mappedDefs.put(type, typeDef);
        }
        return typeDef;
    }

    /**
     * 获取类型代理
     *
     * @param type
     * @return
     */
    Proxy<?> getProxy(byte type) {
        return proxys.get(type);
    }

    /**
     * 获取消息定义
     */
    public byte[] getDescription() {
        if (description == null) {
            synchronized (this) {
                if (description != null) {
                    return description;
                }

                List<Type> all = new ArrayList<>(typeDefs.size() + enumDefs.size());
                // 类型
                List<TypeDef> defs = new ArrayList<TypeDef>(typeDefs.values());
                Collections.sort(defs);
                for (TypeDef t : defs) {
                    all.add(t.getType());
                }
                // 枚举
                List<EnumDef> enmus = new ArrayList<EnumDef>(enumDefs.values());
                Collections.sort(enmus);
                for (EnumDef t : enmus) {
                    all.add(t.getType());
                }
                // 生成定义
                byte[] bytes = this.describe(all);
                // 旧协议
                description = ZlibUtils.zip(bytes);

                try {
                    md5Description = CryptUtils.byte2hex(CryptUtils.md5(description));
                    if (log.isInfoEnabled()) {
                        log.info("协议定义MD5[{}], [{}]字节", md5Description, description.length);
                    }
                } catch (Exception ex) {
                    log.error("MD5", ex);
                    md5Description = "";
                }
            }
        }
        return description;
    }

    /**
     * @param bytes
     * @throws IOException
     */
    void setDescribe(byte[] bytes) throws IOException {
        // 重置枚举定义
        enumIdxs.clear();
        enumDefs.clear();
        enumCurrent.set(0);
        // 重置类型定义
        typeIdxs.clear();
        typeDefs.clear();
        typeCurrent.set(0);
        // 解释定义
        byte[] result = this.describe(bytes);
        description = result;
    }

    /**
     * 获取消息定义MD5串
     */
    public String getDescriptionMD5() {
        if (description == null) {
            // 生成
            getDescription();
        }
        return md5Description;
    }

    /**
     * 对象类型转换
     *
     * @param value
     * @param type
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T convert(Object value, Type type) {
        if (value == null) {
            return (T) value;
        }
        if (type == null) {
            return (T) value;
        }

        if ((type instanceof Class) && TypeUtils.isInstance(value, type)) {
            return (T) value;
        }

        // 变量类型的泛型
        if (type instanceof TypeVariable<?>) {
            return (T) value;
        }

        // 枚举
        if (TypeUtils.isAssignable(type, Enum.class)) {
            Class enumType = (Class<T>) type;
            if (value instanceof Number) {
                T[] enums = (T[]) enumType.getEnumConstants();
                int ordinal = ((Number) value).intValue();
                return enums[ordinal];
            } else if (value instanceof String) {
                return (T) Enum.valueOf(enumType, (String) value);
            }
        }
        return (T) JsonUtils.convertObject(value, type);
    }

    // ----------

    private byte[] describe(Collection<Type> types) {
        // 类型描述
        ByteBuf buf = Unpooled.buffer();
        for (Type type : types) {
            if (TypeUtils.isAssignable(type, Enum.class)) {
                EnumDef typeDef = enumDefs.get(type);
                if (typeDef != null) {
                    typeDef.describe(buf);
                }
            } else {
                TypeDef typeDef = typeDefs.get(type);
                if (typeDef != null) {
                    typeDef.describe(buf);
                }
            }
        }
        int pos = buf.writerIndex();
        byte[] tmp = new byte[pos];
        buf.resetReaderIndex();
        buf.readBytes(tmp);
        buf.release();
        return tmp;
    }

    private byte[] describe(byte[] bytes) throws IOException {
        // 解析格式定义
        byte[] unzip = ZlibUtils.unzip(bytes, 30, TimeUnit.SECONDS);

        ByteBuffer buf = ByteBuffer.wrap(unzip);
        // 类型描述
        while (buf.hasRemaining()) {
            byte flag = buf.get();
            if (flag == 0x00) {
                // 枚举
                EnumDef def = EnumDef.valueOf(buf);
                int code = def.getCode();
                Class<?> clz = def.getType();
                enumIdxs.put(code, def);
                if (clz != null) {
                    enumDefs.put(clz, def);
                }
            } else if (flag == 0x01) {
                // 对象
                TypeDef def = TypeDef.valueOf(buf);
                int code = def.getCode();
                Type clz = def.getType();
                typeIdxs.put(code, def);
                if (clz != null) {
                    typeDefs.put(clz, def);
                }
            }
        }
        return bytes;
    }

    private void init() {
        // 初始化类型代理
        proxys.put(Types.ARRAY, new ArrayProxy());
        proxys.put(Types.BOOLEAN, new BooleanProxy());
        proxys.put(Types.BYTE_ARRAY, new BytesProxy());
        proxys.put(Types.DATE_TIME, new DateProxy());
        proxys.put(Types.ENUM, new EnumProxy());
        proxys.put(Types.MAP, new MapProxy());
        proxys.put(Types.NULL, new NullProxy());
        proxys.put(Types.NUMBER, new NumberProxy());
        proxys.put(Types.OBJECT, new ObjectProxy());
        proxys.put(Types.STRING, new StringProxy());
        proxys.put(Types.COLLECTION, new CollectionProxy());
    }

    // ----------

    public Definition() {
        // 初始化类型代理
        init();
    }

    public Definition(byte[] description) throws IOException {
        init();
        // 类型注册
        setDescribe(description);
    }

    public Definition(Collection<Class<?>> clzs, int startIndex) {
        init();
        // 类型注册
        for (Class<?> clz : clzs) {
            register(clz, startIndex);
            if (startIndex > 0) {
                startIndex++;
            }
        }
    }

}

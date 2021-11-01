package com.pangu.framework.protocol.def;

import com.pangu.framework.protocol.annotation.Ignore;
import com.pangu.framework.utils.reflect.ReflectionUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * 传输类型定义
 *
 * @author Ramon
 */
public class TypeDef implements Comparable<TypeDef> {

    private static Logger log = LoggerFactory.getLogger(TypeDef.class);

    public static TypeDef NULL = TypeDef.valueOf(-1, Object.class);

    // 排序比较器
    private static final Comparator<PropertyDescriptor> CAMPARATOR = new Comparator<PropertyDescriptor>() {
        public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
            return new CompareToBuilder().append(o1.getName(), o2.getName()).toComparison();
        }
    };

    private int code;
    private String name;
    private Type type;
    private List<FieldDef> fields;
    private Constructor<?> construct;

    public static TypeDef valueOf(int code, Class<?> type) {
        Field[] fieldlist = type.getDeclaredFields();
        List<FieldDef> fields = new ArrayList<FieldDef>(fieldlist.length);

        int index = 0;
        List<Field> list = Arrays.asList(fieldlist);
        list.sort(Comparator.comparing(Field::getName));
        for (Field field : list) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            String name = field.getName();
            Type fieldType = field.getGenericType();
            FieldDef fd = FieldDef.valueOf(index, name, fieldType, field);
            fields.add(fd);
            index++;
        }
        return TypeDef.valueOf(code, type, fields);
    }

    public static TypeDef valueOf(int code, Class<?> type, List<FieldDef> fields) {
        TypeDef e = new TypeDef();
        e.code = code;
        if (type == null) {
            e.type = Void.class;
            e.name = Void.class.getName();
            return e;
        }
        e.type = type;
        e.fields = fields;
        e.name = type.getName();

        // 获取默认构造器
        for (Constructor<?> c : type.getDeclaredConstructors()) {
            if (c.getParameterTypes().length == 0) {
                c.setAccessible(true);
                e.construct = c;
                break;
            }
        }

        if (e.construct == null) {
            // throw new IllegalArgumentException("类型[" + type + "]默认构造器不存在...");
            log.info("类型[{}]默认构造器不存在...", type);
        }

        return e;
    }

    public static TypeDef valueOf(ByteBuffer buf) throws IOException {
        // 类型, 类标识, (类名长度, 类名字节), 属性数量, (名字长度, 名字字节)....
        short code = buf.getShort();
        short nLen = buf.getShort();
        byte[] nBytes = new byte[nLen];
        buf.get(nBytes);
        String clzName = new String(nBytes);
        Class<?> clz;
        try {
            clz = Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            // 类型不存在
            log.warn("类型[{}]不存在, 当作HashMap处理");
            clz = null;
        }

        int len = buf.getShort();
        List<FieldDef> fields = new ArrayList<FieldDef>(len);
        for (int i = 0; i < len; i++) {
            // 属性名
            int nField = buf.getShort();
            byte[] aField = new byte[nField];
            buf.get(aField);
            String name = new String(aField);

            Field field;
            Type fieldType;
            if (clz != null) {
                field = ReflectionUtils.findField(clz, name);
                if (field == null) {
                    fieldType = null;
                    log.warn("类型[{}]属性[{}]域不存在", clz, name);
                } else {
                    fieldType = field.getGenericType();
                }
            } else {
                field = null;
                fieldType = null;
            }
            fields.add(FieldDef.valueOf(i, name, fieldType, field));
        }

        return TypeDef.valueOf(code, clz, fields);
    }

    public void describe(ByteBuf buf) {
        // 类型, 类标识, (类名长度, 类名字节), 属性数量, (名字长度, 名字字节), (类型长度, 类型字节)....
        // 类型, 类标识, (类名长度, 类名字节), 属性数量, (名字长度, 名字字节)....
        int code = this.getCode();
        String name = this.getName();
        byte[] defBytes = name.getBytes();
        buf.writeByte((byte) 0x01);
        buf.writeShort((short) code);
        buf.writeShort((short) defBytes.length);
        buf.writeBytes(defBytes);

        List<FieldDef> fields = this.getFields();
        Collections.sort(fields);
        buf.writeShort((short) fields.size());
        for (FieldDef d : fields) {
            byte[] fieldBytes = d.getName().getBytes();
            buf.writeShort((short) fieldBytes.length);
            buf.writeBytes(fieldBytes);
            // byte[] ftBytes = d.getType().getName().getBytes();
            // buf.putShort((short) ftBytes.length);
            // buf.put(ftBytes);
        }
    }

    public Object newInstance() throws Exception {
        if (type == null) {
            return new HashMap<String, Object>();
        }

        if (construct == null) {
            throw new IllegalAccessException("类型[" + type + "]默认构造器不存在...");
        }
        return construct.newInstance();
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<FieldDef> getFields() {
        return fields;
    }

    public Object getValue(Object instance, int index) throws Exception {
        if (index < fields.size()) {
            FieldDef fieldDef = fields.get(index);
            if (fieldDef != null) {
                Object value = fieldDef.getValue(instance);
                if (log.isDebugEnabled()) {
                    log.debug("对象[{}]属性[{}:{}]取值[{}]", new Object[]{instance, index, fieldDef.getName(), value});
                }
                return value;
            }
        }
        return null;
    }

    public void setValue(Object instance, int index, Object value) throws Exception {
        if (index < fields.size()) {
            FieldDef fieldDef = fields.get(index);
            if (fieldDef != null) {
                if (fieldDef.isReadonly()) {
                    log.debug("对象[{}]属性[{}:{}]*只读*无法赋值[{}]",
                            instance, index, fieldDef.getName(), value);
                    return;
                }
                fieldDef.setValue(instance, value);
                if (log.isDebugEnabled()) {
                    log.debug("对象[{}]属性[{}:{}]赋值[{}]", instance, index, fieldDef.getName(), value);
                }
            }
        }
    }

    @Override
    public int compareTo(TypeDef o) {
        return new CompareToBuilder().append(this.code, o.code).append(this.getName(), o.getName()).toComparison();
    }

    @Override
    public String toString() {
        return "TypeDef [" + code + ", " + name + "]";
    }

}

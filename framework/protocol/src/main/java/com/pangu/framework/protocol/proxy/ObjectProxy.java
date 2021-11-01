package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.ObjectProxyException;
import com.pangu.framework.protocol.exception.UnknowSignalException;
import com.pangu.framework.protocol.exception.UnknowTypeDefException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.helpers.MessageFormatter;

import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import com.pangu.framework.protocol.def.FieldDef;
import com.pangu.framework.protocol.def.TypeDef;

public class ObjectProxy extends AbstractProxy<Object> {

    @Override
    public Object getValue(Context ctx, byte flag) {
        ByteBuf buffer = ctx.getBuffer();
        byte type = getFlagTypes(flag);
        if (type != Types.OBJECT) {
            throw new WrongTypeException(Types.OBJECT, type);
        }

        byte signal = getFlagSignal(flag);
        if (signal == 0x00) {
            // #### 0000
            byte tag = buffer.readByte();
            int rawType = readVarInt32(buffer, tag);

            if (rawType == 0) {
                throw new UnknowTypeDefException(rawType);
            }

            // 对象解析
            TypeDef def = ctx.getTypeDef(rawType);
            if (def == null || def.getCode() < 0) {
                if (log.isWarnEnabled()) {
                    log.warn("传输对象类型定义[{}]不存在", type);
                }
                throw new UnknowTypeDefException(rawType);
            }

            List<FieldDef> fields = def.getFields();
            Object obj;
            try {
                // 对象赋值
                obj = def.newInstance();
            } catch (Exception e) {
                String msg = MessageFormatter.format("创建类型[{}]实例异常", def.getName()).getMessage();
                throw new ObjectProxyException(msg, e);
            }
            // 字段数量, 最大255
            int len = 0xFF & buffer.readByte();
            for (int i = 0; i < len; i++) {
                byte fValue = buffer.readByte();
                FieldDef fieldDef = fields.get(i);
                Type clz = fieldDef.getType();
                Object value = ctx.getValue(fValue, clz);
                if (value == null) {
                    continue;
                }
                // 字段赋值
                try {
                    def.setValue(obj, i, value);
                } catch (Exception e) {
                    String msg = MessageFormatter.arrayFormat("对象[{}]实例属性[{}]赋值异常",
                            new Object[]{def.getName(), def.getFields().get(i).getName()}).getMessage();
                    throw new ObjectProxyException(msg, e);
                }
            }
            return obj;
        }
        throw new UnknowSignalException(type, signal);
    }

    @Override
    public void setValue(Context ctx, Object value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.OBJECT;
        Class<? extends Object> type = value.getClass();
        TypeDef def = ctx.getTypeDef(type);
        if (def == null || def.getCode() < 0) {
            if (log.isInfoEnabled()) {
                log.info("传输对象类型定义[{}]不存在", type);
            }
            // 类型定义不存在
            // throw new UnknowTypeDefException(type);
            TypeDef mappedDef = ctx.getMappedDef(type);
            List<FieldDef> fields = mappedDef.getFields();
            int size = fields.size();
            Map<Object, Object> map = new HashMap<Object, Object>(size);
            for (int i = 0; i < size; i++) {
                FieldDef fieldDef = fields.get(i);
                String k = fieldDef.getName();
                Object o;
                try {
                    o = mappedDef.getValue(value, i);
                    if (o != null) {
                        map.put(k, o);
                    }
                } catch (Exception e) {
                    String msg = MessageFormatter.arrayFormat("对象[{}]属性[{}]赋值异常",
                            new Object[]{mappedDef.getClass(), i, e}).getMessage();
                    throw new ObjectProxyException(msg, e);
                }
            }
            ctx.setValue(map);
        } else {
            // #### 0000
            out.writeByte(flag);

            int code = def.getCode();
            putVarInt32(out, code);

            // 字段数量, 最大255
            List<FieldDef> fields = def.getFields();
            int size = fields.size();
            if (size > 0xFF) {
                String msg = MessageFormatter.arrayFormat("对象[{}]属性数量[{}]超过最大值",
                        new Object[]{def.getClass(), size}).toString();
                throw new ObjectProxyException(msg, new RuntimeException(msg));
            }
            out.writeByte((byte) size);
            // 遍历属性
            for (int i = 0; i < size; i++) {
                Object o;
                try {
                    o = def.getValue(value, i);
                    ctx.setValue(o);
                } catch (Exception e) {
                    String msg = MessageFormatter.arrayFormat("对象[{}]属性[{}]:[{}]赋值异常",
                            new Object[]{def.getName(), i, def.getFields().get(i).getName(), e}).getMessage();
                    throw new ObjectProxyException(msg, e);
                }
            }

        }
    }
}

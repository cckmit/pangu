package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.UnknowSignalException;
import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapProxy extends AbstractProxy<Map<Object, Object>> {

    @Override
    public Map<Object, Object> getValue(Context ctx, byte flag) {
        // 非明确定义的对象类型，均当做MAP解析

        ByteBuf buffer = ctx.getBuffer();
        byte type = getFlagTypes(flag);
        if (type != Types.MAP) {
            throw new WrongTypeException(Types.MAP, type);
        }

        byte signal = getFlagSignal(flag);
        if (signal == 0x00) {
            // 对象解析
            try {
                // 对象赋值
                Map<Object, Object> result = new HashMap<Object, Object>();
                // 字段数量
                byte tag = buffer.readByte();
                int len = readVarInt32(buffer, tag);
                for (int i = 0; i < len; i++) {
                    byte fKey = buffer.readByte();
                    Object key = ctx.getValue(fKey);
                    byte fValue = buffer.readByte();
                    Object value = ctx.getValue(fValue);
                    // 字段赋值
                    result.put(key, value);
                }
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        throw new UnknowSignalException(type, signal);
    }

    @Override
    public void setValue(Context ctx, Map<Object, Object> value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.MAP;
        // #### 0000
        out.writeByte(flag);
        Set<Entry<Object, Object>> entrySet = value.entrySet();
        // 字段数量
        int size = entrySet.size();
        putVarInt32(out, size);
        for (Entry<Object, Object> e : entrySet) {
            ctx.setValue(e.getKey());
            ctx.setValue(e.getValue());
        }
    }
}

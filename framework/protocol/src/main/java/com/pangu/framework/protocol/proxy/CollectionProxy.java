package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.exception.WrongTypeException;
import com.pangu.framework.protocol.Context;
import com.pangu.framework.protocol.Types;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.Collection;

public class CollectionProxy extends AbstractProxy<Collection<?>> {

    @Override
    public Collection<?> getValue(Context ctx, byte flag) {
        byte type = getFlagTypes(flag);
        if (type != Types.COLLECTION) {
            throw new WrongTypeException(Types.COLLECTION, type);
        }
        byte signal = getFlagSignal(flag);

        // 读取数组
        byte arrayFlag = (byte) (Types.ARRAY | signal);
        Object[] array = (Object[]) ctx.getValue(arrayFlag);
        return Arrays.asList(array);
    }

    @Override
    public void setValue(Context ctx, Collection<?> value) {
        ByteBuf out = ctx.getBuffer();
        byte flag = Types.COLLECTION;
        Object[] array = value.toArray();
        // #### 0000
        out.writeByte(flag);
        int len = array.length;
        putVarInt32(out, len);
        for (Object obj : array) {
            ctx.setValue(obj);
        }
    }
}

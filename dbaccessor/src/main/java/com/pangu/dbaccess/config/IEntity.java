package com.pangu.dbaccess.config;

import com.pangu.dbaccess.service.EntityService;
import com.pangu.framework.utils.json.JsonUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public interface IEntity<PK> {

    Set<String> dirty();

    @Proxy
    default void persist() {
        throw new UnsupportedOperationException("必须通过EntityService#load方法返回的增强类调用");
    }

    @Proxy
    default void init(Map<String, Object> columns, FieldDesc[] fields) {
        if (columns == null) {
            return;
        }
        for (FieldDesc fieldDesc : fields) {
            Object value = columns.remove(fieldDesc.getColumnName());
            if (value == null) {
                continue;
            }
            Type fileType = fieldDesc.getFileType();
            try {
                Object o = JsonUtils.convertObject(value, fileType);
                fieldDesc.getField().set(this, o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

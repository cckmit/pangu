package com.pangu.dbaccess.config;

import com.pangu.dbaccess.anno.Idx;
import com.pangu.dbaccess.anno.Unique;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EntityConfigParser {

    public static EntityConfig parse(Class<?> clz) {
        Entity annotation = clz.getAnnotation(Entity.class);
        if (annotation == null) {
            throw new IllegalStateException("实体类型必须添加@javax.persistence.Entity注解");
        }
        EntityConfig entityConfig = new EntityConfig();
        String tableName = annotation.name();
        if (StringUtils.isEmpty(tableName)) {
            tableName = clz.getSimpleName();
        }
        entityConfig.setTableName(tableName);
        Field[] declaredFields = clz.getDeclaredFields();
        List<FieldDesc> fieldDesc = new ArrayList<>(declaredFields.length);
        entityConfig.setFieldDesc(fieldDesc);
        for (Field field : declaredFields) {
            int modifiers = field.getModifiers();
            if ((modifiers & (Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT)) != 0) {
                continue;
            }
            String fieldName = field.getName();
            if (modifiers != 0 && (modifiers & (Modifier.PROTECTED)) == 0) {
                throw new IllegalStateException("实体" + clz.getName() + "字段名" + fieldName + "修饰服必须为protect或者default");
            }
            Column column = field.getAnnotation(Column.class);
            String colName = fieldName;
            if (column != null) {
                String curColumnName = column.name();
                if (StringUtils.isNotEmpty(curColumnName)) {
                    colName = curColumnName;
                }
            }
            field.setAccessible(true);
            fieldDesc.add(new FieldDesc(fieldName, colName, field.getGenericType(), field));

            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation != null) {
                entityConfig.setIdName(fieldName);
            }
            Idx idx = field.getAnnotation(Idx.class);
            if (idx != null) {
                entityConfig.setSingleRegionName(fieldName);
            }
            Unique unique = field.getAnnotation(Unique.class);
            if (unique != null) {
                entityConfig.addUniqueField(fieldName);
            }
        }
        entityConfig.checkValid();

        return entityConfig;
    }
}

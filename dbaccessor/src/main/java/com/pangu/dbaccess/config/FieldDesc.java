package com.pangu.dbaccess.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

@Getter
@AllArgsConstructor
public class FieldDesc {

    private final String fieldName;

    private final String columnName;

    private final Type fileType;

    private final Field field;

}

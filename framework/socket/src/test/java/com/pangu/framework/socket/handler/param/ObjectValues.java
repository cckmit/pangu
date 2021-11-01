package com.pangu.framework.socket.handler.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ObjectValues {
    private int age;
    private String name;
    private Map<String, Integer> seg;
}
package com.pangu.logic.module.battle.service.buff.utils;

import com.pangu.logic.module.battle.model.AlterType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class CalValues {
    private final Map<AlterType, Number> values;
}

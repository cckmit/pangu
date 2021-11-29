package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;

@Transable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class UnitBuildInfo implements Cloneable {
    //  模型信息
    private ModelInfo model;
    //  属性数值
    private HashMap<UnitValue, Long> values;
    //  机率集合
    private HashMap<UnitRate, Double> rates;
    //  初始化状态
    private int state;
    // 站位顺序
    private int sequence;
    //  可用技能配置
    private String[] skills;
    //  被动效果配置
    private String[] passives;
    //  初始化效果配置
    private String[] inits;

    public UnitBuildInfo copy() {
        UnitBuildInfo info = clone();
        info.model = model.copyOrClone(null);
        if (values != null) {
            info.values = new HashMap<>(values);
        }
        if (rates != null) {
            info.rates = new HashMap<>(rates);
        }
        info.skills = ArrayUtils.clone(skills);
        info.passives = ArrayUtils.clone(passives);
        info.inits = ArrayUtils.clone(inits);
        return info;
    }

    @Override
    protected UnitBuildInfo clone() {
        try {
            return (UnitBuildInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            log.warn("拷贝UnitInfo失败", e);
            return new UnitBuildInfo();
        }
    }
}

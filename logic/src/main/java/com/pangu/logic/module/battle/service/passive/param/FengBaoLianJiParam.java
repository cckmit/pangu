package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class FengBaoLianJiParam {
    //指定区域生效
    private AreaParam area;
    //替换的技能id
    private String skillId;
    //在技能循环第Integer段执行String特效
    private Map<Integer, String> trigger;
}

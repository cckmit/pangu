package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class AddShieldAndRelateBuffParam {

    private String shieldBuffId;

    private List<String> buffs = Collections.emptyList();

}

package com.pangu.logic.module.battle.service.passive.param;

import lombok.Data;

/**
 * 猎豹之息
 * 自身血量低于50%时，每降低5%的生命值，攻击速度提升1%。
 * 2级：生命值低于30%时，每降低5%的生命值，攻速提升2%
 * 3级：生命值低于30%时，每次攻击都会获得20%的吸血效果
 * 4级：生命值低于10%时，免疫一切控制效果
 */
@Data
public class LieBaoZhiXiParam {

    /**
     * 可以触发吸血比率
     */
    private int canSuckHpRate;

    /**
     * 吸血比率
     */
    private double suckHp;

    private int aaPercent;
    private int aaModel;
    private int aaValue;

    private int bbPercent;
    private int bbModel;
    private int bbValue;

    private int immuneRate;
}

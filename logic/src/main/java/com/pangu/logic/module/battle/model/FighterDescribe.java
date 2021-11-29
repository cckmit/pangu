package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

@Transable
@Data
public class FighterDescribe {

    // 名字
    private String name;

    //头像
    private int face;

    //当前穿戴的头像框
    private int wear;

    // 等级
    private int level;

    // 战力
    private long fight;

    // VIP
    private int vip;

    // 可选id
    private String id;

    public static FighterDescribe of(String name, int level, int face, int wear, String id, int vip, long fight) {
        FighterDescribe d = new FighterDescribe();
        d.name = name;
        d.face = face;
        d.wear = wear;
        d.level = level;
        d.id = id;
        d.vip = vip;
        d.fight = fight;
        return d;
    }
}

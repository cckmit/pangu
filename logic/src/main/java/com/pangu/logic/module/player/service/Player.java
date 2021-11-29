package com.pangu.logic.module.player.service;

import com.pangu.dbaccess.anno.Unique;
import com.pangu.logic.module.account.model.Sex;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Player {

    @Id
    private long id;

    // 玩家姓名
    @Unique
    private String name;

    // 角色经验池
    private long exp;

    // 是否禁言true：禁言，false：正常
    private boolean block ;

    // 是否需要重命名
    private boolean rename;

    // 性别
    private Sex sex;

    // 等级
    private int level ;

    /* 战力信息 */
    private long fight;

    //玩家历史最大战力
    private long maxFight;


    public static Player of(long id, String roleName, Sex sex) {
        Player p = new Player();
        p.id = id;
        p.name = roleName;
        p.sex = sex;
        p.level = 1;
        return p;
    }
}
package com.pangu.logic.module.battle.service.convertor;

import com.pangu.logic.module.battle.model.FighterType;
import com.pangu.logic.module.battle.service.core.Fighter;

/**
 * 战斗单位转换器
 */
public interface FighterConvertor<T> {

    /**
     * 根据标识符获取战斗单位集合
     *
     * @param id         标识符(标识符由具体实现类解析)
     * @param isAttacker 是否攻击方(不是攻击方就是防守方)
     * @return
     */
    Fighter convert(T id, boolean isAttacker, int index);

    /**
     * 判断战斗场数
     *
     * @param id
     * @return
     */
    int getBattleTimes(T id);

    /**
     * 判断胜利场次
     *
     * @param id
     * @return
     */
    default Integer getWinTimes(T id) {
        return null;
    }

    ;

    /**
     * 获取该转换器对应的战斗单位类型
     *
     * @return
     */
    FighterType getType();

}

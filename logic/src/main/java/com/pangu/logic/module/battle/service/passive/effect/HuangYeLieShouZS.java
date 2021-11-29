package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.HuangYeLieShouZSParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 荒野猎手·莉莎专属装备
 * 1：敌人每次使用大招时，自身提升20%暴击，持续5秒
 * 10：敌人每次释放大招时，获得一个自身攻击力150%的护盾
 * 20：敌人每次释放大招时，获得一个自身攻击力300%的护盾
 * 30：敌人每次使用大招时，自身提升30%暴击，持续5秒
 * @author Kubby
 */
@Component
public class HuangYeLieShouZS implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.HUANG_YE_LIE_SHOU_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        if (owner.getEnemy() != attacker.getEnemy()) {
            return;
        }

        HuangYeLieShouZSParam param = passiveState.getParam(HuangYeLieShouZSParam.class);

        if (!StringUtils.isBlank(param.getBuffId())) {
            BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        }

        if (!StringUtils.isBlank(param.getShieldBuffId())) {
            BuffFactory.addBuff(param.getShieldBuffId(), owner, owner, time, skillReport, null);
        }
    }
}

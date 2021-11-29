package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.effect.IntervalRangeAura;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.WuShuangTieBiParam;
import com.pangu.logic.module.battle.service.passive.param.ZhanZhengNvShenZSPassiveParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 战争女神·贝罗妮卡专属装备
 * 1：举盾期间，自身周围的敌人，所有回能-25%
 * 10：举盾期间，自身周围的敌人，所有回能-40%
 * 20：举盾期间，自身周围的敌人，所有回能-65%
 * 30：连续处于自己身边超过6秒的敌人，会眩晕1秒
 *
 * @author Kubby
 */
@Component
public class ZhanZhengNvShenZSPassive implements SkillReleasePassive, OwnerDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.ZHAN_ZHENG_NV_SHEN_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }

        if (owner.getPassiveStateByType(PassiveType.WU_SHUANG_TIE_BI).size() <= 0) {
            return;
        }

        ZhanZhengNvShenZSPassiveParam param = passiveState.getParam(ZhanZhengNvShenZSPassiveParam.class);

        PassiveState wushuangTieBiPassive = owner.getPassiveStateByType(PassiveType.WU_SHUANG_TIE_BI).get(0);
        WuShuangTieBiParam wushuangTieBiParam = wushuangTieBiPassive.getParam(WuShuangTieBiParam.class);
        String tag = skillState.getTag();
        /* 举盾开始 */
        if (wushuangTieBiParam.getStartSkillTag().equals(tag)) {
            /* 给自己添加降低周围回能的光环BUFF */
            BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        }
        /* 举盾结束 */
        if (wushuangTieBiPassive.getAddition(int.class, 0) > time
                || (wushuangTieBiParam.getEndSkillTag() != null && wushuangTieBiParam.getEndSkillTag().equals(tag))) {
            /* 给自己移除降低周围回能的光环BUFF */
            BuffFactory.removeBuffState(param.getBuffId(), owner, time);
        }
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time,
                    Context context) {
        ZhanZhengNvShenZSPassiveParam param = passiveState.getParam(ZhanZhengNvShenZSPassiveParam.class);

        /* 给自己移除降低周围回能的光环BUFF */
        BuffFactory.removeBuffState(param.getBuffId(), owner, time);

        /* 眩晕光环BUFF重置 */
        List<BuffState> list = owner.getBuffBySettingId(param.getXuanYunBuffId());
        for (BuffState buffState : list) {
            IntervalRangeAura.IntervalRangeAuraAddition addition = buffState
                    .getAddition(IntervalRangeAura.IntervalRangeAuraAddition.class);
            if (addition != null) {
                addition.clear();
            }
        }
    }
}

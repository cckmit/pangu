package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemRemove;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.XingZhiShouHuParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 星之守护:
 * 自身血量低于20%时,将占星球回收到自己身上,移除所有DEBUFF和异常状态,并获得护体效果2秒,持续时间内受到的伤害降低60%,每秒恢复自身损失生命的10%,能量恢复100点,每45秒触发1次
 * 2级:持续时间提升至3秒
 * 3级:血量低于30%时就会触发
 * 4级:每秒恢复自身损失生命提升至15%
 */
@Component
public class XingZhiShouHu implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final XingZhiShouHuParam param = passiveState.getParam(XingZhiShouHuParam.class);
        if (owner.getHpPct() >= param.getTriggerPct() || damage >= 0) return;
        //移除所有负面效果
        BuffFactory.removeBuffsByDispelType(owner, time, DispelType.HARMFUL);
        owner.removeStateByIfHarmful(true);
        //添加BUFF
        for (String buffId : param.getBuffIds()) {
            BuffFactory.addBuff(buffId, owner, owner, time, skillReport, null);
        }
        //计算cd
        passiveState.addCD(time);
        //让飞行道具飞回自身，服务端逻辑为移除道具
        final LinkedList<ItemAdd> items = owner.getItems();
        if (items == null || items.isEmpty()) return;
        skillReport.add(time, owner.getId(), new ItemRemove(items.get(0).getId()));
        owner.removeItem(items.get(0));
        //通知专属装备被动(如果有)普攻已强化
        final List<PassiveState> passiveStates = owner.getPassiveStateByType(PassiveType.ZHAN_XING_MO_OU_ZS);
        if (CollectionUtils.isEmpty(passiveStates)) {
            return;
        }
        final ZhanXingMoOuZS passive = PassiveFactory.getPassive(PassiveType.ZHAN_XING_MO_OU_ZS);
        final PassiveState zsPassive = passiveStates.get(0);
        final ZhanXingMoOuZS.Addition addition = passive.getAddition(zsPassive);
        addition.setKillMpRecoverState(ZhanXingMoOuZS.Addition.State.CONSUMABLE);
        addition.setNormalAtkState(ZhanXingMoOuZS.Addition.State.CONSUMABLE);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.XING_ZHI_SHOU_HU;
    }
}

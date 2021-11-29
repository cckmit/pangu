package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.AddBuffAction;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ZhanXiZhiFengZSParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 斩风之息·武专属装备
 * 1：每次释放斩在前之后，永久提升2%爆伤和暴击率，最多5层
 * 10：每次释放斩在前之后，永久提升3%爆伤和暴击率，最多5层
 * 20：每次释放斩在前之后，永久提升4%爆伤和暴击率，最多5层
 * 30：每次释放斩在前之后，永久提升5%爆伤和暴击率，最多6层
 *
 * @author Kubby
 */
@Component
public class ZhanXiZhiFengZS implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.ZHAN_XI_ZHI_FENG_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }

        ZhanXiZhiFengZSParam param = passiveState.getParam(ZhanXiZhiFengZSParam.class);

        if (!skillState.getTag().equals(param.getSkillTag())) {
            return;
        }

        List<BuffState> currs = owner.getBuffBySettingId(param.getBuffId());

        if (currs.size() >= param.getOverlayLimit()) {
            return;
        }

        AddBuffAction action = AddBuffAction.of(owner, owner, Collections.singletonList(param.getBuffId()), true, time, skillReport);
        owner.addTimedAction(action);
    }
}

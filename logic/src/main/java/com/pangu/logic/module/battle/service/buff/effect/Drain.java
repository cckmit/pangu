package com.pangu.logic.module.battle.service.buff.effect;

import com.dianping.cat.util.StringUtils;
import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.ctx.OwnerTargetCtx;
import com.pangu.logic.utils.ExpressionHelper;
import org.springframework.stereotype.Component;

/**
 * 施法者定时吸收BUFF持有者的生命值
 */
@Component
public class Drain implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.DRAIN;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final String exp = state.getParam(String.class);
        if (StringUtils.isEmpty(exp)) {
            return;
        }

        final Unit caster = state.getCaster();
        OwnerTargetCtx ctx = new OwnerTargetCtx(time, caster, unit);
        Number ret = ExpressionHelper.invoke(exp, Number.class, ctx);
        long damage = ret.longValue();

        final BuffReport buffReport = state.getBuffReport();
        final Context context = new Context(caster);
        //buff持有者生命值扣减
        context.addValue(unit, AlterType.HP, -damage);
        buffReport.add(time, unit.getId(), Hp.of(-damage));
        //buff施法者生命值增加
        context.addValue(caster, AlterType.HP, damage);
        buffReport.add(time, caster.getId(), Hp.of(damage));
        context.execute(time, buffReport);
    }
}

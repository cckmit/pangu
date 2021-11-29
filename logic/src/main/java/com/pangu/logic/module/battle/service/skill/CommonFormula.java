package com.pangu.logic.module.battle.service.skill;

import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.ctx.OwnerTargetCtx;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CommonFormula {

    //  所有者
    private static final String OWNER = "owner";
    //  目标
    public static final String TARGET = "target";

    //  物理技能是否成功的计算公式
    @Static("FIGHT:HP_M_DAMAGE_HIT")
    private Formula isHitFormula;

    private static CommonFormula INSTANCE;

    @PostConstruct
    void init() {
        INSTANCE = this;
    }

    public static boolean isHit(int time, Unit owner, Unit target) {
        OwnerTargetCtx ownerTargetCtx = new OwnerTargetCtx(time, owner, target);
        Double rate = ExpressionHelper.invoke(INSTANCE.isHitFormula.getContent(), Double.class, ownerTargetCtx);
        return RandomUtils.isHit(rate);
    }
}

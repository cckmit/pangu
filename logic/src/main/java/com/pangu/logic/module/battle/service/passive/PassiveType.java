package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.model.HeroRaceType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.passive.param.*;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.param.SummonSkillParam;
import com.pangu.logic.module.battle.service.skill.param.UnyieldParam;

import static org.apache.commons.lang3.ArrayUtils.toArray;

/**
 * 被动类型
 */
public enum PassiveType {
    //  根据变身状态替换技能
    REPLACE_SKILL_BY_TRANSFORM_STATE(ReplaceSkillByTransformStateParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    //  释放技能增加能量
    ADD_EP_ON_SKILL_RELEASE(AddEpOnSkillReleaseParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  对召唤单元进行初始化操作
    INIT_SUMMONED(InitSummonedParam.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    //  友方死亡时召唤一些单元
    SUMMON_ON_OWNER_DIE(SummonSkillParam.class, Phase.OWNER_DIE, Priority.END + 1),
    //  死亡后移除自身召唤物
    REMOVE_SUMMONED_ON_SUMMONER_DIE(String.class, Phase.OWNER_DIE, Priority.END + 1),
    //  持有者血量百分比低于某个阈值时，治疗一些目标并添加BUFF
    CURE_ON_OWNER_HP_DOWN(CureOnOwnerHpDownParam.class, Phase.HP_DOWN, Priority.END),
    //  根据目标身上指定tag counter的层数进行增伤
    DMG_UP_ON_COUNTER(DmgUpOnCounterParam.class, Phase.ATTACK, Priority.NORMAL),
    //  被击杀时将身上的COUNTER层数传播给附近的单元
    PROPAGATE_COUNTER(PropagateCounterParam.class, Phase.OWNER_DIE, Priority.END),
    //  击杀时添加状态
    ADD_STATE_ON_KILLING(AddStateOnKillingParam.class, Phase.DIE, Priority.NORMAL),
    //  女神忠犬变大
    NV_SHEN_ZHONG_QUAN_TRANSFORM(NvShenZhongQuanTransformParam.class, Phase.ATTACK, Priority.NORMAL),
    //  在循环系技能持续期间参与击杀目标，延长该循环系技能的持续时间（执行次数）
    HUO_LI_ZHI_YUAN_EXTEND(Integer.class, toArray(Phase.ATTACK, Phase.DIE), Priority.NORMAL),
    //  当目标未死亡时重新释放一次当前的技能
    SKILL_REPEAT_ON_TARGET_SURVIVE(String.class, toArray(Phase.ATTACK, Phase.DIE, Phase.SKILL_SELECT), Priority.NORMAL),
    //  工匠大师传承被动[雷电一击]
    THUNDER_HIT(ThunderHitParam.class, toArray(Phase.ATTACK, Phase.DIE), Priority.NORMAL),
    //  飞机传承被动[无限导弹]
    UNLIMITED_MISSILE_WORK(UnlimitedMissileWorkParam.class, toArray(Phase.SKILL_SELECT, Phase.SKILL_RELEASE), Priority.NORMAL),
    //  生命值低于某个百分比阈值后进行操作，当生命值高于某个百分比阈值后复位操作
    BUFF_CAST_ON_HP_DOWN(BuffCastOnHpDownParam.class, Phase.HP_DOWN, Priority.NORMAL),
    //  闪避成功时进行一些操作
    EFFECT_ON_DODGE(EffectOnDodgeParam.class, Phase.BE_ATTACK_BEFORE, Priority.NORMAL),
    //  有条件的更新下一个技能
    CONDITIONALLY_UPDATE_SKILL(ConditionallyUpdateSkillParam.class, Phase.HP_DOWN, Priority.NORMAL),
    //  提升主动技能（包括BUFF）所造成的治疗量
    ACTIVE_CURE_UP(CureUpParam.class, Phase.HP_DOWN, Priority.NORMAL),
    //  将当前攻击者的注意力转移给其他单元
    SHIFT_ATTENTION(ShiftAttentionParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  根据目标身上特定BUFF层数，增幅我方指定TAG技能的伤害
    DMG_UP_ON_TARGET_BUFF_COUNT(DmgUpByTargetBuffCountParam.class, Phase.ATTACK, Priority.NORMAL),
    //  治疗时为目标添加状态
    STATE_ADD_ON_CURE(StateAddParam.class, Phase.RECOVER_TARGET, Priority.NORMAL),
    //  控制敌人或敌人死亡时，回复自身生命
    RECOVER_ON_CONTROL_OR_ENEMY_DIE(RecoverOnControlOrEnemyDieParam.class, toArray(Phase.DIE, Phase.STATE_ADD), Priority.NORMAL),
    //  根据技能标签，对目标额外施加击退效果
    REPEL_BY_SKILL_TAG(RepelBySkillTagParam.class, Phase.ATTACK, Priority.NORMAL),
    //  通用条件增伤被动
    CONDITIONALLY_DMG_UP(ConditionallyDmgUpParam.class, Phase.ATTACK, Priority.NORMAL),
    //  间接参与击杀时添加增益
    BUFF_CAST_ON_INDIRECTLY_KILL(BuffCastOnIndirectlyKillParam.class, toArray(Phase.ATTACK, Phase.DIE), Priority.NORMAL),
    //  命中不同目标时造成额外伤害
    DMG_UP_ON_TARGET_SHIFT(DmgUpOnTargetShiftParam.class, Phase.ATTACK, Priority.NORMAL),
    //  受到伤害时移动目标
    MOVE_WHEN_DMG(MoveWhenDmgParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  释放技能时驱散范围内敌人的增益并造成伤害
    DMG_AND_DISPEL_BUFF_ON_SKILL_RELEASE(DmgAndDispelBuffOnSkillReleaseParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  极冰女皇传承被动：残血回复+无敌
    JIBINGNVHUANG_UNYIELD(JiBingNvHuangUnYieldParam.class, Phase.HP_DOWN, Priority.END),
    //  基于exp表达式的通用效果
    CONDITIONALLY_EFFECT(ConditionallyEffectParam.class, toArray(Phase.ATTACK, Phase.DAMAGE), Priority.NORMAL),
    //  根据id前缀替换技能
    SKILL_REPLACE_BY_ID_PREFIX(SkillReplaceByIdPrefixParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    //  根据目标身上指定Counter层数，提供额外增伤，并为自己添加减伤BUFF
    LIE_YAN_TIAN_HUO(LieYanTianHuoParam.class, Phase.ATTACK, Priority.NORMAL),
    //  通用技能释放阶段处理器
    OWNER_EFFECT_BY_TAG_SKILL_RELEASE(OwnerEffectByTagSkillReleaseParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  治疗加深
    REC_DEEPEN(DamageDeepenParam.class, Phase.RECOVER_TARGET, Priority.END),
    //  受到暴击时为自己添加BUFF
    BUFF_CAST_WHEN_BE_CRIT(BuffCastWhenBeCritParam.class, Phase.HP_DOWN, Priority.END),
    //  受到控制之前执行某些行为
    DO_STH_WHEN_CONTROLLED(BuffCastWhenControlledParam.class, Phase.BE_STATE_ADD, Priority.NORMAL),
    //  受到控制时为自己添加BUFF
    BUFF_CAST_WHEN_CONTROLLED(BuffCastWhenControlledParam.class, Phase.DAMAGE, Priority.END),
    //  每命中一个处于异常状态中的单元，增加暴击率，暴击时重置
    BAO_LIE(double.class, Phase.ATTACK, Priority.NORMAL),
    //  敌军死亡时，为全体友军添加BUFF
    @Deprecated
    YONG_ZHAN_LISTENER(String.class, Phase.DIE, Priority.NORMAL),
    //  友军被暴击时为其添加BUFF
    @Deprecated
    TIAN_CI_LISTENER(BuffCastWhenBeCritParam.class, Phase.HP_DOWN, Priority.NORMAL),
    //  释放大招时有概率无视目标防御
    DUAN_BING(DuanBingParam.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    //  受到治疗时有概率为自己添加BUFF
    BUFF_ADD_WHEN_RECOVERED(BuffAddWhenRecoveredParam.class, Phase.RECOVER, Priority.NORMAL),
    //  普攻时有概率额外为自己添加护盾
    JIAN_DUN(JianDunParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  释放大招时有概率额外造成AOE伤害
    TIAN_NU(TianNuParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  每次攻击会将一部分伤害囤积起来，直到某个目标的生命值低于囤积伤害时，直接将该目标斩杀
    SHI_KONG_XING_ZHE_ZS(ShiKongXingZheZSParam.class, toArray(Phase.ATTACK, Phase.HP_DOWN, Phase.DIE), Priority.END),
    //  攻击时，根据目标身上指定buff的层数来执行不同操作
    ADDITIONAL_EFFECT_BY_COUNTER(AdditionalEffectByCounterParam.class, Phase.ATTACK_END, Priority.END),
    //  免疫指定种族以外的伤害
    DMG_IGNORE_EXCEPT_CERTAIN_RACE(HeroRaceType.class, Phase.HP_DOWN, Priority.END),
    //  于HpChange阶段生效的不屈，对buff造成的伤害也有效
    UNYIELDING_ON_HP_CHANGE(UnyieldingOnHpChangeParam.class, Phase.HP_DOWN, Priority.END),
    //  敌方合计受伤X次后，调整数值
    UNIT_HP_CHANGE_TIMES_LISTENER(UnitHpChangeTimesListenerParam.class, Phase.HP_DOWN, Priority.HIGHEST),
    //  友方英雄每次死亡时，修改幸存者的数值
    CHANGE_SURVIVORS_VALUES_WHEN_FRIEND_HERO_DIE_LISTENER(ChangeSurvivorsValuesWhenHeroDieListenerParam.class, Phase.DIE, Priority.END),
    //  对非克制目标造成伤害时，降低伤害
    CHANGE_DMG_WHEN_NON_COUNTER_LISTENER(ChangeDmgWhenNonCounterListenerParam.class, Phase.HP_DOWN, Priority.END),
    //  下次释放大招时增伤
    SHUI_ZHI_SHENG_NV(ShuiZhiShengNvParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  与持有特定BUFF的友军分摊伤害
    YONG_GU_ZHI_BAO(YongGuZhiBaoParam.class, Phase.DAMAGE, Priority.END),
    //  受到致死伤害时，按公式回复血量
    COME_BACK_KID(String.class, Phase.OWNER_DIE, Priority.NORMAL),
    //  对持有护盾的目标造成额外伤害
    SHIELD_COUNTER(Double.class, Phase.ATTACK, Priority.NORMAL),
    //  释放X次技能后，执行一些操作
    ACTION_AFTER_SKILL_RELEASES_LISTENER(ActionAfterSkillReleasesListenerParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  源石神像监听器
    YSSX_LISTENER(YSSXParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  致命伤害免疫并分摊
    SLAY_DMG_SHARE(SlayDmgShareParam.class, Phase.OWNER_DIE, Priority.HIGHEST),
    //  受到治疗时提升自身伤害
    DMG_UP_WHEN_CURED(DmgUpWhenCuredParam.class, Phase.HP_DOWN, Priority.NORMAL),
    //  受到攻击时有概率延迟伤害
    DMG_DELAY(DmgDelayParam.class, toArray(Phase.DAMAGE, Phase.HP_DOWN), Priority.END),
    //  释放技能时降低敌方怒气
    MP_CHANGE_WHEN_RELEASE_SKILL(MpCutWhenReleaseSkillParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  必中
    RATE_UP_WHEN_MISS(RateUpWhenMissParam.class, toArray(Phase.ATTACK_BEFORE, Phase.SKILL_RELEASE), Priority.NORMAL),
    //  所受伤害越多，面板提升越高
    KUANG_BAO(KuangBaoParam.class, Phase.HP_DOWN, Priority.NORMAL),
    //  释放特定技能时，无视目标防御和无敌状态，造成真实伤害与增伤
    WU_JIN_SHA_LU(WuJinShaLuParam.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    //  将造成伤害作为dot值
    ZUI_ZHONG_SHEN_PAN(ZuiZhongShenPanParam.class, toArray(Phase.ATTACK, Phase.HP_DOWN), Priority.NORMAL),
    //  释放普攻时有一定概率立即更新下一次行动
    UPDATE_SKILL_BY_TAG_SKILL_RELEASE(UpdateSkillByTagSkillReleaseParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  每X次普攻，击退目标并添加debuff
    @Deprecated
    HUO_LI_YA_ZHI(HuoLiYaZhiParam.class, toArray(Phase.SKILL_SELECT, Phase.SKILL_RELEASE, Phase.ATTACK), Priority.NORMAL),
    //  移除目标身上的增益，并根据增益的数量扣减目标能量
    RONG_LU_LIE_YAN(Integer.class, Phase.ATTACK, Priority.NORMAL),
    //  敌方每次释放指定类型的技能时，修改自身计数器
    HUI_MIE_DAN_MU(HuiMieDanMuParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  根据目标的英雄的类型附带不同效果
    QI_YUAN_DE_NA_HAN(QiYuanDeNaHanParam.class, Phase.ATTACK, Priority.NORMAL),
    //  当生命值低于某个百分比时，回复攻击力百分比的生命值，并给自身添加一个buff
    RONG_LU_ZHI_XIN_ZS(RongLuZhiXinZSParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  消耗道具回复生命值低于某个比例的友方，当道具消耗完毕后提交一个行为在指定cd后重新生成道具
    XIAO_SE_ZHI_QIN_ZS(XiaoSeZhiQinZSParam.class, toArray(Phase.HP_DOWN), Priority.NORMAL),
    //  暴击时一定概率给目标添加印记层数
    CANG_BAI_ZHI_PU_ZS(CangBaiZhiPuZSParam.class, toArray(Phase.ATTACK), Priority.NORMAL),
    //  释放完大招后的下一次普攻额外增伤并回能
    YUE_ZHI_ZHAN_JI(YueZhiZhanJiParam.class, toArray(Phase.ATTACK, Phase.SKILL_RELEASE), Priority.NORMAL),
    //  每X次普攻增伤、添加debuff。队友死亡时向击杀者添加debuff
    YUE_ZHI_SHEN_PAN(YueZhiShenPanParam.class, toArray(Phase.SKILL_SELECT, Phase.DIE, Phase.SKILL_RELEASE, Phase.ATTACK), Priority.NORMAL),
    //  根据目标身上的印记数量来决定生效效果
    YUE_REN_CAI_JUE(YueRenCaiJueParam.class, Phase.ATTACK, Priority.NORMAL),
    //  根据目标当前能量进行增伤
    ZHUO_XIN_ZHI_GUANG_ZS(ZhuoXinZhiGuangParamZS.class, Phase.ATTACK, Priority.NORMAL),
    //  指定技能每命中一个目标，回复能量
    ZHUO_XIN_YI_ZHI(Integer.class, Phase.ATTACK, Priority.NORMAL),
    //  队友释放大招时回复最大百分比生命值，溢出部分转化为能量
    SHA_HAI_SI_SHEN_ZS(ShaHaiSiShenZSParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  召唤物在攻击和回复模式来回切换
    NV_SHEN_ZHONG_QUAN_MODE_SWITCH(NvShenZhongQuanModeSwitchParam.class, toArray(Phase.SKILL_SELECT, Phase.ATTACK, Phase.SKILL_RELEASE), Priority.NORMAL),
    //  阵亡时杀死一个召唤物并回复最大百分比生命值
    NV_SHEN_ZHONG_QUAN_SACRIFICE(NvShenZhongQuanSacrificeParam.class, Phase.OWNER_DIE, Priority.NORMAL),
    //  指定技能命中友方添加增益、造成伤害吸血
    SHA_ZHI_BIAN_TA(ShaZhiBianTaParam.class, Phase.ATTACK, Priority.NORMAL),
    //  持有特定buff的敌人攻击自身时伤害降低
    LIU_SHA_PEN_YONG(Double.class, Phase.DAMAGE, Priority.LOWEST),
    //  命中目标添加debuff，释放技能为自己添加buff，攻击存在特定debuff的敌人必定暴击
    JIU_CHENG_CI_MI_ZS(JiuChengCiMiZSParam.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE, Phase.ATTACK), Priority.NORMAL),
    //  根据目标的距离提升暴击率
    ZHI_MING_JU_SHA(ZhiMingJuShaParam.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    //  对首个命中的目标之外的其他目标造成的伤害，衰减至指定百分比。
    GUAN_CHUAN_SHE_JI(Double.class, Phase.ATTACK, Priority.NORMAL),
    //  每普攻X次，第X+1次必定暴击
    RUO_DIAN_SHE_JI(RuoDianSheJiParam.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    //  根据身上的counter层数计算易伤比例
    LVE_SHI_ZHI_YA_ZS_VULNERABLE(Double.class, Phase.DAMAGE, Priority.NORMAL),
    //  每次攻击目标更新目标身上的buff，当目标身上的buff满层时造成额外伤害
    LVE_SHI_ZHI_YA_ZS(LveShiZhiYaParam.class, Phase.ATTACK, Priority.NORMAL),
    //  每存在一层充能，替换一次普攻
    CONG_LIN_ZHI_LI(String.class, Phase.SKILL_SELECT, Priority.NORMAL),
    //  受到低于最大生命值一定比例的伤害时，减伤
    YE_SHOU_ZHI_QU(YeShouZhiQuParam.class, Phase.DAMAGE, Priority.LOWEST),
    //  根据敌方阵容路由将要发动的技能
    MO_RI_SHEN_PAN_ROUTER(String.class, Phase.SKILL_SELECT, Priority.NORMAL),
    //  根据法球状态，提升自身伤害、动态添加buff
    ZHU_XING_LING_ZHU_ZS(ZhuXingLingZhuZSParam.class, toArray(Phase.ATTACK, Phase.SKILL_RELEASE), Priority.NORMAL),
    //  受到伤害后击退周围目标
    SHOU_HU_FA_QIU(ShouHuFaQiuParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  被攻击数次后召唤一个单位。召唤单位存活时不进行统计
    HAN_BING_JING_LING(HanBingJingLingParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  当对目标释放特定技能时，同时为它添加一个buff
    MU_GUANG_XIU_NV_ZS(String.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    //  每阵亡一个友军，回复一次能量
    MP_RECOVER_PER_FRIEND_DIE(Integer.class, Phase.DIE, Priority.NORMAL),
    //  击杀召唤物回能，普攻有概率添加异常并增伤
    JI_BING_NV_HUANG_ZS(JiBingNvHuangZSParam.class, Phase.ATTACK, Priority.NORMAL),
    //  血线低于某个百分比阈值时添加控制异常
    BING_FENG_SHI_JIE(BingFengShiJieParam.class, Phase.ATTACK, Priority.NORMAL),
    //  X概率为至多Y个单位添加/更新buff
    BING_SHUANG_AI_QU(BingShuangAiQuParam.class, Phase.ATTACK, Priority.NORMAL),
    //  免疫并反弹部分技能伤害
    NV_HUANG_ZHI_WEI(NvHuangZhiWeiParam.class, Phase.DAMAGE, Priority.LOWEST),
    //  带标记的友方击杀敌方时，替换自身的下次普攻。圣光洗礼专属。
    SHENG_GUANG_XI_LI_ZS(ShengGuangXiLiZSParam.class, toArray(Phase.DIE, Phase.SKILL_RELEASE, Phase.SKILL_SELECT, Phase.ATTACK, Phase.HP_DOWN), Priority.NORMAL),
    //  带标记敌方分摊伤害。灾祸羊灵专属
    ZAI_HUO_YANG_LING_ZS(Double.class, Phase.DAMAGE, Priority.LOWEST),
    //  每次释放技能后，替换下次普攻，并在下次普攻时增伤、回能。击杀时额外回能
    ZHAN_XING_MO_OU_ZS(ZhanXingMoOuZSParam.class, toArray(Phase.DIE, Phase.SKILL_RELEASE, Phase.ATTACK, Phase.SKILL_SELECT), Priority.NORMAL),
    //  血线低于某个百分比时，移除自身所有负面效果，给自己添加buff
    XING_ZHI_SHOU_HU(XingZhiShouHuParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  技能每轮执行时，每经过一个敌人伤害递减。配合同名主动效果使用
    @Deprecated
    XING_BAO(XingBaoParam.class, Phase.ATTACK, Priority.NORMAL),
    //  每命中一个敌人，对敌人添加buff，给自己回复能量。配合同名主动效果使用
    XING_GUANG_NI_LIU(XingGuangNiLiuParam.class, Phase.ATTACK, Priority.NORMAL),
    //  增伤对应主动技能效果缓存的目标。
    DA_DI_SI_LIE(DaDiSiLieParam.class, Phase.ATTACK, Priority.NORMAL),
    //  MP抵扣伤害
    MP_SHIELD(MpShieldParam.class, Phase.DAMAGE, Priority.END),
    //  指定区域存在目标时，替换普攻
    CHECK_AND_REPLACE(CheckAndReplaceParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    //  被攻击时有一定概率反击并触发一个效果
    FAN_JI_ZHI_LIAN(FanJiZhiLianParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  有一定概率接住镰刀，接住镰刀后给自身添加buff并回复能量
    HUI_XUAN_FEI_LIAN(HuiXuanFeiLianParam.class, Phase.ATTACK_END, Priority.NORMAL),
    //  技能重复命中只造成部分伤害。配合同名主动效果使用
    SHENG_MING_SHOU_GE(Double.class, Phase.ATTACK, Priority.NORMAL),
    //  每次释放技能添加buff，可配置触发概率、触发类型、补偿概率、释放目标、BUFF种类
    ADD_BUFF_WHEN_SKILL_RELEASE(AddBuffWhenAttackParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    //  普攻暴击增加感电层数
    LEI_TING_ZHI_ZHU_ZS(LeiTingZhiZhuZSParam.class, toArray(Phase.ATTACK, Phase.DAMAGE), Priority.NORMAL),
    //  伤害加深，每多（少）一个人增加X伤害
    LEI_TING_SHEN_FA(LeiTingShenFaParam.class, Phase.ATTACK, Priority.NORMAL),
    //  指定区域不存在敌人时，禁止释放大招；多段技能可指定在第N段调用特殊效果
    FENG_BAO_LIAN_JI(FengBaoLianJiParam.class, toArray(Phase.ATTACK, Phase.SKILL_SELECT), Priority.NORMAL),
    //  技能在多次生命周期间，连续命中同一个敌人时，造成最大生命值百分比的伤害，并触发其他技能效果
    LIE_MO_ZHI_SHI(LieMoZhiShiParam.class, Phase.ATTACK, Priority.NORMAL),
    //  技能在一次生命周期中，多次命中同一个敌人时伤害提升，需要指定技能配合实现
    ZI_RAN_ZHI_NU(Double.class, Phase.ATTACK, Priority.NORMAL),
    //  击杀回能，残血暴击
    YI_JI_ZHI_MING(YuXueParam.class, toArray(Phase.ATTACK_BEFORE, Phase.DIE, Phase.SKILL_RELEASE), Priority.NORMAL),
    //  暴击时恢复生命上限百分比的血量和固定值的能量
    SHENG_GUANG_ZHI_HUI(YuXueParam.class, Phase.ATTACK, Priority.NORMAL),
    //  通用死亡被动，持有者死后调用一个技能
    LAST_WORD(LastWordParam.class, Phase.OWNER_DIE, Priority.NORMAL),
    //  优先级更低的亡语被动
    REAL_LAST_WORD(LastWordParam.class, Phase.OWNER_DIE, Priority.END + 1),
    //  受击链，传递所受伤害的百分比给所有队友
    VULNERABLE_CHAIN(Double.class, Phase.DAMAGE, Priority.NORMAL),
    //  每命中一个敌人，恢复一定比例的已损失生命
    YIN_XUE(YinXueParam.class, Phase.ATTACK, Priority.NORMAL),
    //  细粒度吸血
    SUCK_HP_BY_SKILL_TYPE(SuckHpBySkillTypeParam.class, Phase.ATTACK, Priority.NORMAL),
    //  受击伤害加深
    VULNERABLE(VulnerableParam.class, Phase.DAMAGE, Priority.NORMAL),
    //  龙魂本源
    LONG_HUN_BEN_YUAN(String.class, Phase.OWNER_DIE, Priority.NORMAL),
    //  深渊魔龙
    SHEN_YUAN_MO_LONG(ShenYuanMoLongParam.class, toArray(Phase.SKILL_SELECT, Phase.DIE, Phase.ATTACK_BEFORE, Phase.ATTACK), Priority.NORMAL),
    //  械力反震，受击时一定概率对攻击方造成 被动持有者攻击力倍率的伤害，添加 任意异常
    XIE_LI_FAN_ZHEN(XieLiFanZhenParam.class, Phase.DAMAGE, Priority.NORMAL),
    // RateReplaceNormalSkill的增强版，可配置额外配置补偿概率
    ACC_RATE_REPLACE_NORMAL_SKILL(AccRateReplaceNormalSkillParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    // 每次造成伤害添加BUFF，可配置触发概率、触发类型、补偿概率、释放目标、BUFF种类、
    ADD_BUFF_WHEN_ATTACK(AddBuffWhenAttackParam.class, Phase.ATTACK, Priority.NORMAL),
    // 场上有单位死亡时，给场上任意单位添加任意Buff，可叠加
    UNIT_DIE_ADD_BUFF(UnitDieAddBuffParam.class, Phase.DIE, Priority.NORMAL),
    // 普攻增伤带异常，可配置增伤比例和异常类型
    CHEN_MO_ZHI_MAO(ChenMoZhiMaoParam.class, Phase.ATTACK, Priority.NORMAL),
    // 致命连矛
    ZHI_MING_LIAN_MAO(ZhiMingLianMaoParam.class, toArray(Phase.DIE, Phase.ATTACK), Priority.NORMAL),
    // 选择技能时添加被动
    SKILL_SELECT_ADD_PASSIVE(SkillSelectAddPassiveParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    // 无声突袭
    WU_SHENG_TU_XI(WuShengTuXiParam.class, Phase.DAMAGE, Priority.END),
    // 狂野血脉
    KUANG_YE_XUE_MAI(KuangYeXueMaiParam.class, Phase.ATTACK, Priority.HIGHEST),
    // 血战到底
    LIE_BAO_ZHI_XI(LieBaoZhiXiParam.class, Phase.DAMAGE, Priority.NORMAL),
    // 蛮角之撞
    MAN_JIAO_ZHI_ZHUANG(ManJiaoZhiZhuangParam.class, toArray(Phase.DAMAGE, Phase.SKILL_SELECT, Phase.ATTACK), Priority.NORMAL),
    // 蛮牛血脉
    MAN_NIU_XUE_MAI(ManNiuXueMaiParam.class, Phase.DAMAGE, Priority.HIGHEST),
    // 伤害全部转化为自身生命
    DAMAGE_TO_HP(Double.class, Phase.DAMAGE, Priority.LOWEST),
    // 血量溢出值转换为护盾
    HP_TO_SHIELD(double.class, Phase.RECOVER, Priority.LOWEST),
    // 流星之剑
    LIU_XING_ZHI_JIAN(String[].class, Phase.ATTACK, Priority.NORMAL),
    //  不屈(保留1点血)
    UNYIELD(UnyieldParam.class, Phase.DAMAGE, Priority.END),
    //  吸血
    SUCK(Double.class, Phase.ATTACK, Priority.END),
    // 追击(就是你了)
    JIU_SHI_NI_LE(JiuShiNiLeParam.class, toArray(Phase.HP_DOWN, Phase.DIE), Priority.NORMAL),
    // 召唤的单元造成的伤害，会给给召唤物补充血量
    SUMMON_HP(Double.class, Phase.ATTACK, Priority.NORMAL),
    // 灵魂震荡
    LING_HUN_ZHEN_DANG(LingHunZhenDangParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    // 灵魂吸收
    LING_HUN_XI_SHOU(LingHunXiShouParam.class, Phase.DIE, Priority.NORMAL),
    // 致死一击
    ZHI_SI_YI_JI(ZhiSiYiJiParam.class, toArray(Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 亡魂汲取
    WANG_HUN_JI_QU(WangHunJiQuParam.class, Phase.DIE, Priority.NORMAL),
    // 冥魂再生
    MING_HUN_ZAI_SHENG(MingHunZaiShengParam.class, Phase.OWNER_DIE, Priority.END + 1),
    // 伤害加深
    DAMAGE_DEEPEN(DamageDeepenParam.class, Phase.ATTACK, Priority.NORMAL),
    // 亡灵唤醒
    WANG_LING_HUAN_XING(WangLingHuanXingParam.class, toArray(Phase.ATTACK, Phase.DIE), Priority.NORMAL + 10),
    //  格挡
    BLOCK(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  致命
    DEADLY(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  斩杀
    SLAY(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  荆棘
    REFLECT(ReflectParam.class, Phase.DAMAGE, Priority.LOWEST),
    //  庇佑(BUFF)
    BLESSING1(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  庇佑(VALUE)
    BLESSING2(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  免疫死亡
    IMMUNE_DEAD(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  变身
    TRANSFORM(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  分身
    CLONE(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  临死一击
    DEAD_ATTACK(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  回光盾
    LIGHT_SHIELD(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  无光之盾
    DARK_SHIELD(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  延迟复活
    DELAY_REVIVE(String.class, Phase.DAMAGE, Priority.NORMAL),
    //  反击技能
    SKILL_REFLECT(String.class, Phase.DAMAGE, Priority.NORMAL),

    // buff移除buff
    BUFF_REMOVE(String.class, Phase.DAMAGE, Priority.NORMAL),
    // 释放大招时触发
    SPACE_SKILL_BUFF(String.class, Phase.DAMAGE, Priority.NORMAL),
    // 血量低于百分比时，触发
    HP_PERCENT(String.class, Phase.DAMAGE, Priority.NORMAL),
    // 骨王之怒
    GU_WANG_ZHI_NU(String.class, Phase.DAMAGE, Priority.NORMAL),
    // 浴血
    YU_XUE(YuXueParam.class, Phase.ATTACK, Priority.NORMAL),
    // 神明之躯
    SHEN_MING_ZHI_QU(Double.class, Phase.DAMAGE, Priority.NORMAL),
    //伤害减少
    DAMAGE_REDUCTION(DieUnitReduceDamageParam.class, Phase.DAMAGE, Priority.NORMAL),
    //血量越低伤害越高
    HP_CHANGE_DAMAGE(HpChangeDamageParam.class, Phase.ATTACK, Priority.NORMAL),
    //击杀后添加属性
    KILL_ADD_VALUES(KillAddValuesParam.class, Phase.DIE, Priority.NORMAL),
    //敌方释放技能后添加buff
    RELEASE_SKILL_ADD_BUFFS(ReleaseSkillAddBuffsParam.class, Phase.SKILL_RELEASE, Priority.NORMAL),
    // 替换普攻
    REPLACE_NORMAL_SKILL(String.class, Phase.SKILL_SELECT, Priority.NORMAL),
    // 有条件地替换普攻
    CONDITIONALLY_REPLACE_SKILL(ConditionallyReplaceSkillParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    // 荣耀之力
    RONG_YAO_ZHI_LI(RongYaoZhiLiParam.class, Phase.ATTACK, Priority.HIGHEST),
    // 狮皇之影
    RATE_REPLACE_NORMAL_SKILL(RateReplaceNormalSkillParam.class, Phase.SKILL_SELECT, Priority.NORMAL),
    // 每次造成伤害添加buff
    ATTACK_ADD_BUFFS(AttackAddBuffsParam.class, Phase.ATTACK, Priority.NORMAL),
    // 每次造成伤害更新buff
    ATTACK_UPDATE_BUFF(BuffUpdateParam.class, Phase.ATTACK, Priority.NORMAL),
    // 魔穿
    MO_CHUAN(MoChuanParam.class, Phase.ATTACK_BEFORE, Priority.HIGHEST),
    //偷取或削弱受到伤害的单元属性
    STEAL_VALUES(StealValuesParam.class, Phase.ATTACK, Priority.NORMAL),
    //友军死后给自己添加属性
    FRIEND_DIE_ADD_VALUES(DefaultAddValueParam.class, Phase.DIE, Priority.NORMAL),
    //稻草娃娃 使随机一名敌人受到的伤害增加100%，持续15秒。该单位在持续时间内死亡时会将该效果随机传递给另一名敌人。
    DCWW(Double.class, new Phase[]{Phase.DAMAGE, Phase.OWNER_DIE}, Priority.NORMAL),
    //死神雕像 受到死神青睐 生命值低于15%直接死亡
    DEATH(Double.class, Phase.ATTACK_END, Priority.END),
    //攻击链 传递当前攻击伤害的15%给其他所有敌方英雄
    ATTACK_CHAIN(Double.class, Phase.ATTACK, Priority.NORMAL),
    //带条件校验的攻击链
    CONDITIONALLY_ATTACK_CHAIN(ConditionallyAttackChainParam.class, Phase.ATTACK, Priority.NORMAL),
    // 鸟嘴医生跟随对象死亡后，切换一个
    MU_SHI_FOLLOW(MuShiFollowPassiveParam.class, Phase.DIE, Priority.LOWEST),
    // 无双光环
    WU_SHUANG_GUANG_HUAN(WuShuangGuangHuanPassiveParam.class, Phase.DIE, Priority.NORMAL),
    // 无双铁壁
    WU_SHUANG_TIE_BI(WuShuangTieBiParam.class, toArray(Phase.DAMAGE, Phase.SKILL_RELEASE), Priority.LOWEST),
    //复活并给自己加BUFF
    REVIVE(ReviveParam.class, Phase.OWNER_DIE, Priority.END),
    //血量低时给自己添加BUFF
    LOW_HP_ADD_BUFF(LowHpAddBuffsParam.class, Phase.DAMAGE, Priority.END),
    //攻击暴击后给自己添加
    CRIT_ADD_BUFFS(String[].class, Phase.ATTACK_END, Priority.NORMAL),
    //被友军治疗时给自己添加一个属性
    BE_RECOVER_ADD_VALUES(DefaultAddValueParam.class, Phase.RECOVER, Priority.NORMAL),
    //死亡后平分MP给队友
    DIVIDE_MP_AFTER_DEATH(Double.class, Phase.OWNER_DIE, Priority.NORMAL),
    // 倔强的爱
    JUE_JIANG_DE_AI(JueJiangDeAiParam.class, Phase.DAMAGE, Priority.LOWEST),
    // 魂灵护盾
    HUN_LING_HU_DUN(HunLingHuDunParam.class, Phase.HP_DOWN, Priority.LOWEST),
    // 魂灵祝福
    HUN_LING_ZHU_FU(HunLingZhuFuParam.class, Phase.HP_DOWN, Priority.NORMAL),
    // 普攻释放多少次后替换普攻
    TIMES_REPLACE_NORMAL_SKILL(TimesReplaceNormalSkillParam.class, toArray(Phase.SKILL_SELECT, Phase.SKILL_RELEASE), Priority.NORMAL),
    // 凶恶践踏
    XIONG_E_JIAN_TA(XiongEJianTaParam.class, Phase.ATTACK, Priority.NORMAL),
    //友方死亡后给自己添加一个BUFF
    FRIEND_DIE_ADD_BUFFS(String[].class, Phase.DIE, Priority.NORMAL),
    //血量低于多少时给自己添加一些属性
    LOW_HP_ADD_VALUES(LowHpAddValuesParam.class, new Phase[]{Phase.DAMAGE, Phase.RECOVER}, Priority.LOWEST),
    //减少受到来自指定布阵位置的伤害
    DECREASE_DAMAGE_FROM_SEQUENCE(DecreaseDamageFromSequenceParam.class, Phase.DAMAGE, Priority.NORMAL),
    //治疗友方单元添加其MP
    RECOVER_FRIEND_ADD_MP(RecoverFriendAddMpParam.class, Phase.RECOVER_TARGET, Priority.NORMAL),
    //超过6%最大生命值的伤害 降低75%
    DECREASE_OVERFLOW_DAMAGE(DecreaseOverflowDamageParam.class, Phase.DAMAGE, Priority.LOWEST),
    // 技能召唤时给召唤物添加BUFF
    ADD_BUFF_WHEN_SUMMON_SKILL(AddBuffWhenSummonSkillParam.class, Phase.ATTACK_BEFORE, Priority.NORMAL),

    // 孤魂摆渡人·拜尔斯专属装备
    GU_HUN_BAI_DU_REN_ZS(GuHunBaiDuRenZSParam.class, toArray(Phase.ATTACK_BEFORE, Phase.HP_DOWN), Priority.NORMAL),
    // 星辰射手·莎凡娜专属装备
    XING_CHEN_SHE_SHOU_ZS(XingChenSheShouZSParam.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 工匠大师·吉拉德专属装备
    GONG_JIANG_DA_SHI_ZS(GongJiangDaShiZSParam.class, toArray(Phase.RECOVER, Phase.OWNER_DIE), Priority.NORMAL),
    // 森林絮语·玛法达专属装备
    SEN_LIN_ZHI_YU_ZS(SenLinZhiYuZSParam.class, toArray(Phase.ATTACK), Priority.NORMAL),
    // 爱神之箭·波托斯专属装备
    AI_SHEN_ZHI_JIAN_ZS(AiShenZhiJianZSParam.class, toArray(Phase.SKILL_RELEASE, Phase.OWNER_DIE), Priority.NORMAL),
    // 荒野猎手·莉莎专属装备
    HUANG_YE_LIE_SHOU_ZS(HuangYeLieShouZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 银白之刺·利昂专属装备
    YIN_BAI_ZHI_CI_ZS(YinBaiZhiCiZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 暗影魔主·奥古斯丁专属装备
    AN_YING_MO_ZHU_ZS(AnYingMoZhuZSParam.class, toArray(Phase.SKILL_SELECT, Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 蛮海主祭·普鲁特专属装备
    MAN_HAI_ZHU_JI_ZS(ManHaiZhuJiZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 夜枭之刃·艾莲娜专属装备
    YE_XIAO_ZHI_REN_ZS(YeXiaoZhiRenZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 亡灵骨王·克劳狄斯专属装备
    WANG_LING_GU_WANG_ZS(WangLingGuWangZSParam.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 诡术巫医·奥尔萨专属装备
    GUI_SHU_WU_YI_ZS(GuiShuWuYiZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 海妖公主·卡莉安娜专属装备
    HAI_YAO_GONG_ZHU_ZS(HaiYaoGongZhuZSPassiveParam.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE, Phase.DAMAGE), Priority.LOWEST),
    // 飞行技师·比佛利专属装备
    FEI_XING_JI_SHI_ZS(String.class, toArray(Phase.DAMAGE), Priority.NORMAL),
    // 撕裂长矛·特朗格尔专属装备
    SI_LIE_CHANG_MAO_ZS(SiLieChangMaoZSParam.class, toArray(Phase.SKILL_SELECT, Phase.SKILL_RELEASE, Phase.ATTACK), Priority.HIGHEST),
    // 疯狂博士·威尔金斯专属装备
    FENG_KUANG_BO_SHI_ZS(FengKuangBoShiZSParam.class, toArray(Phase.ATTACK_BEFORE, Phase.OWNER_DIE), Priority.NORMAL),
    // 大地守护·安泰专属装备
    DA_DI_SHOU_HU_ZS(DaDiShouHuZSPassiveParam.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE, Phase.DAMAGE), Priority.NORMAL),
    // 深渊屠夫·席恩专属装备
    SHEN_YUAN_TU_FU_ZS(ShenYuanTuFuZSParam.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 初始化，用于添加属性
    INIT_VALUES(InitValuesParam.class, Phase.INIT, Priority.NORMAL),
    // 战争女神·贝罗妮卡专属装备
    ZHAN_ZHENG_NV_SHEN_ZS(ZhanZhengNvShenZSPassiveParam.class, toArray(Phase.SKILL_RELEASE, Phase.OWNER_DIE), Priority.NORMAL),
    // 技能属性临时变更
    SKILL_ATTR_ALTER(SkillAttributeAlterParam.class, toArray(Phase.ATTACK_BEFORE, Phase.BE_ATTACK_BEFORE), Priority.NORMAL),
    // 复苏
    FU_SU(FuSuParam.class, toArray(Phase.HP_DOWN), Priority.NORMAL),
    // 当受伤时添加BUFF
    ADD_BUFF_WHEN_DAMAGE(AddBuffWhenDamageParam.class, toArray(Phase.DAMAGE), Priority.NORMAL),
    // 释放技能时添加属性
    ADD_VALUES_WHEN_SKILL(AddValuesWhenSkillParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    //受到几次攻击后死亡
    BE_ATTACK_DIE(Integer.class, Phase.DAMAGE, Priority.NORMAL),
    // 起开
    QI_KAI(QiKaiParam.class, Phase.DAMAGE, Priority.NORMAL),
    // 顽劣之火·贝拉技能：燃魂
    RAN_HUN(RanHunParam.class, toArray(Phase.ATTACK, Phase.DIE), Priority.NORMAL),
    // 顽劣之火·贝拉技能：传火
    CHUAN_HUO(ChuanHuoParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 顽劣之火·贝拉专属装备
    WAN_LIE_ZHI_HUO_ZS(WanLieZhiHuoZSParam.class, toArray(Phase.BE_ATTACK_BEFORE, Phase.ATTACK), Priority.NORMAL),
    // 斩风之息·武技能：风之息
    FENG_ZHI_XI(FengZhiXiParam.class, toArray(Phase.SKILL_SELECT, Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 斩风之息·武专属装备
    ZHAN_XI_ZHI_FENG_ZS(ZhanXiZhiFengZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 陆海霸主·巴达克技能：火枪乍现
    HUO_QIANG_ZHA_XIAN(HuoQiangZhaXianParam.class, toArray(Phase.ATTACK), Priority.NORMAL),
    // 风暴女皇·艾琳技能：风之链条
    FENG_ZHI_LIAN_TIAO(String.class, toArray(Phase.DAMAGE, Phase.OWNER_DIE), Priority.NORMAL),
    // 蝴蝶仙子·莉亚娜技能：迷蝶花海
    MI_DIE_HUA_HAI(String.class, toArray(Phase.DAMAGE), Priority.NORMAL),
    // 蝴蝶仙子·莉亚娜技能：迷踪之蝶
    @Deprecated
    MI_ZONG_ZHI_DIE(MiZongZhiDieParam.class, toArray(Phase.DAMAGE), Priority.NORMAL),
    MI_ZONG_ZHI_DIE_REMAKE(MiZongZhiDieRemakeParam.class, Phase.DAMAGE, Priority.NORMAL),
    // 蝴蝶仙子·莉亚娜专属装备
    HU_DIE_XIAN_ZI_ZS(HuDieXianZiZSParam.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 唤星女神·维纳斯技能：星环
    XING_HUAN(String[].class, toArray(Phase.RECOVER_TARGET), Priority.NORMAL),
    // 恶魔猎人·康斯坦丁专属装备
    E_MO_LIE_REN_ZS(int.class, toArray(Phase.SKILL_RELEASE, Phase.ATTACK_BEFORE), Priority.NORMAL),
    // 灵魂收割者·路西法专属装备
    LING_HUN_SHOU_GE_ZHE_ZS(String.class, toArray(Phase.SKILL_RELEASE), Priority.NORMAL),
    // 维京战魂BOSS技能：画地为牢
    HUA_DI_WEI_LAO(HuaDiWeiLaoPassiveParam.class, toArray(Phase.DAMAGE), Priority.NORMAL),
    // 斗志燃烧
    DOU_ZHI_RAN_SHAO(DouZhiRanShaoParam.class, toArray(Phase.SKILL_SELECT), Priority.NORMAL),
    //召唤物击杀单元后 给召唤者回蓝
    SUMMON_MP(Integer.class, Phase.DIE, Priority.NORMAL),
    //对方血量越低伤害越高
    ATTACK_DEEPEN_BY_TARGET_HP(AttackDeepenByTargetHpParam.class, Phase.ATTACK, Priority.NORMAL),
    //无视百分比防御
    IGNORE_DEFENCE(Double.class, Phase.ATTACK_BEFORE, Priority.NORMAL),
    ;

    // 参数类型
    private final Class<?> clz;

    // 被动执行阶段
    private final Phase[] phases;

    // 执行优先级
    private final int priority;

    PassiveType(Class<?> clz, Phase phases, int priority) {
        this(clz, toArray(phases), priority);
    }

    PassiveType(Class<?> clz, Phase[] phases, int priority) {
        this.clz = clz;
        this.phases = phases;
        this.priority = priority;
    }

    public Phase[] getPhases() {
        return phases;
    }

    public int getPriority() {
        return priority;
    }

    public Class<?> getClz() {
        return clz;
    }
}

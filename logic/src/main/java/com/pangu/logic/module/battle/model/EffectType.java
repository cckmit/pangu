package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.skill.TwinsMeleeSpaceParam;
import com.pangu.logic.module.battle.service.skill.param.*;
import com.pangu.framework.protocol.annotation.Transable;

/**
 * 主动技能效果类型
 */
@Transable
public enum EffectType {
    //  双子召唤
    TWINS_SUMMON(TwinsSummonParam.class),
    //  双子近战连刺
    TWINS_MELEE_SPIKE(TwinsMeleeSpikeParam.class),
    //  双子远程大招
    TWINS_RANGED_SPACE(TwinsRangedSpaceParam.class),
    //  双子近战大招
    TWINS_MELEE_SPACE(TwinsMeleeSpaceParam.class),
    //  夜宵之刃回收道具效果
    YE_XIAO_ZHI_REN_ITEM_RECYCLE(YeXiaoZhiRenItemRecycleParam.class),
    //  交换生命
    HP_EXCHANGE(String.class),
    //  弹射弹道
    BOUNCY_BULLET(BouncyBulletParam.class),
    //  被动替换
    PASSIVE_REPLACE(PassiveReplaceParam.class),
    //  将指定主动技能沿特定分支升级
    ACTIVE_SKILL_UPGRADE(ActiveSkillUpgradeParam.class),
    //  追踪当前目标
    TRACE_TARGET(int.class),
    //  连续三次攻击，统共上一层火，第三次击飞
    NEZHA_TRIPLE_ATK(NeZhaTripleAtkParam.class),
    //  不执行任何效果
    NONE,
    //  执行随机策略的循环次数选择器
    RANDOM_TOTAL_EXEC_TIMES_SETTER(Integer[].class),
    //  释放时锁定两个目标，一段时间后施加晕眩
    AIM_REPORT_GENERATOR(Integer.class),
    //  对特定区域持续施加影响
    EFFECT_AREA(EffectAreaParam.class),
    //  构建施法柱
    BUILD_SERVANT_UNIT(BuildServantUnitParam.class),
    //  水之圣女驱散
    SHUI_ZHI_SHENG_NV(Integer.class),
    //  湮灭魔眼
    YAN_MIE_MO_YAN(YanMieMoYanParam.class),
    //  审判罡风
    SHEN_PAN_GANG_FENG(ShenPanGangFengParam.class),
    //  自杀效果，无视自身一切状态强行使自己死亡，不触发任何被动
    @Deprecated
    SUICIDE,
    //  目标朝向设置器
    FACE_TARGET_SETTER,
    //  打印bestCircle区域战报
    BEST_CIRCLE_AREA_REPORT_GENERATOR,
    //  打印bestCircle区域战报，每个技能只打印一次
    BEST_CIRCLE_AREA_REPORT_ONCE_GENERATOR,
    //  平移至出场时靠近的边界
    HORIZONTALLY_MOVE_TO_BIRTH_VERTICAL_BORDER,
    //  平移至较近的垂直边界
    HORIZONTALLY_MOVE_TO_NEARER_VERTICAL_BORDER,
    //  瞬移至最近的角落
    RETREAT_TO_NEAREST_CORNER,
    //  朝最最虚弱的目标及其附近造成伤害
    XING_BAO_NEW(XingBaoNewParam.class),
    //  水平方向对齐faceTarget
    FACE_TARGET_HORIZONTAL_ALIGN_MOVE(Integer.class),
    //  即死
    KILL(KillParam.class),
    //  用于产生ScheduledSkillUpdateAction
    SCHEDULED_SKILL_ACTION_GENERATE(ScheduledSkillActionGenerateParam.class),
    //  在施法者当前坐标生成X个道具
    XIAO_SE_ZHI_QIN_ZS(Integer.class),
    //  以自身为圆心制造一个结界，对首次进入结界的角色造成伤害。持续对结界内部的角色添加debuff
    CANG_BAI_JIE_JIE(CangBaiJieJieParam.class),
    //  对印记最多的目标造成魔法伤害
    YUE_REN_CAI_JUE(AttackParam.class),
    //  使被成功驱逐的目标坠落在指定落点并对其附近造成伤害
    ZHUO_GUANG_ZHI_MEN_MOVE(ZhuoGuangZhiMenParam.class),
    //  筛选并驱逐符合条件的目标，提交一个延迟行为在指定延时后使目标坠落在指定落点
    ZHUO_GUANG_ZHI_MEN_EXILE(ZhuoGuangZhiMenParam.class),
    //  从指定被动中获取造成的伤害
    NV_SHEN_ZHONG_QUAN_RECOVER(Double.class),
    //  为指定被动充能
    CONG_LIN_ZHI_LI(Integer.class),
    //  对目标区域造成范围伤害，后以目标区域为起点作矩形，矩形区域持续对内部目标造成伤害
    MO_RI_SHEN_PAN_METEOR(MoRiShenPanMeteorParam.class),
    //  朝密集区域投掷一个固定的电磁球，每秒降低周围单位的能量，倒计时结束后造成范围伤害
    MO_RI_SHEN_PAN_ELECTRIC(MoRiShenPanElectricParam.class),
    //  以当前自身位置为圆心释放一个固定的结界，为结界内部的友方添加buff
    GUANG_ZHI_LING_YU(GuangZhiLingYuParam.class),
    //  造成一次伤害后，立即执行另一个技能
    BING_FENG_SHI_JIE(BingFengShiJieParam.class),
    //  使飞行道具朝人最密集的区域发动直线伤害，对路径上的敌人造成伤害
    @Deprecated
    XING_BAO(DaDiSiLieParam.class),
    //  更改目标坐标
    MOVE(Point.class),
    //  对敌方最多的圆心投掷一个道具，并对范围内敌人造成伤害，短暂延迟后将范围内目标拉向圆心
    XING_GUANG_NI_LIU(XingGuangNiLiuParam.class),
    //  以矩形范围攻击人最多的区域，并将位于矩形中轴附近的目标缓存，配合被动效果增伤
    DA_DI_SI_LIE(DaDiSiLieParam.class),

    //  吸收MP
    MP_SUCK(Integer.class),

    //  根据不同阵营选择执行不同效果，ZHI_YU_HE_XIAN的抽象升级版。可自由配置需要执行的效果
    EXE_EFFECT_BY_PARTY(ExeEffectByPartyParam.class),

    //  能量变更
    MP_CHANGE(Integer.class),
    //  能量变更（按当前MP的百分比变更）
    MP_CHANGE_PCT(Double.class),
    // 能量回满（立即生效）
    MP_FULL,

    //  优先攻击一次技能生命周期中未被命中过的目标，由于伤害计算的特殊性。已命中目标通过被动来统计
    SHENG_MING_SHOU_GE(ShengMingShouGeParam.class),

    //  更新BUFF
    BUFF_UPDATE(BuffUpdateParam.class),

    //  随机攻击一名目标，支持不可重复选取
    ZI_RAN_ZHI_NU(ZiRanZhiNuParam.class),

    //  对指定区域内的目标释放buff
    BU_XIU_LING_YU(BuXiuLingYuParam.class),

    //  命中友军则治疗，命中敌军则伤害
    ZHI_YU_HE_XIAN(ZhiYuHeXianParam.class),

    //  在指定范围内随机移动
    RANDOM_MOVE(WuWeiXuanFengParam.class),

    //  变身
    TRANSFORM(TransformParam.class),

    //  命中后对目标单位造成持续伤害
    DAMAGE_OVER_TIME(DamageOverTimeParam.class),

    //  电磁炸弹
    DIAN_CI_ZHA_DAN(DianCiZhaDanParam.class),

    //  释放多个BUFF
    BUFFS_CAST(String[].class),
    //  释放多个BUFF（由Target释放给Owner）
    BUFFS_CAST_BY_TARGET(String[].class),

    //  电磁力场
    DIAN_CI_LI_CHANG(DianCiLiChangParam.class),

    //  HP物理伤害
    HP_P_DAMAGE(DamageParam.class),

    //  HP法术伤害
    HP_M_DAMAGE(DamageParam.class),

    //  造成物理与魔法攻击中较高一方的伤害
    HP_H_DAMAGE(DamageParam.class),

    /**
     * 根据表达式计算伤害
     */
    EXPR_DAMAGE(String.class),

    /**
     * 按血量最大值百分比算伤害
     */
    HP_PCT_DAMAGE(HpPctDamageParam.class),

    // 增加被动技能
    PASSIVE_ADD(String[].class),
    //  属性修改
    VALUES_DAMAGE(ValuesDamageParam.class),
    //  带条件判断的属性修改效果
    CONDITIONAL_VAL_DMG(ConditionalValDmgParam.class),
    //  击退
    REPEL(Integer.class),
    //  驱散BUFF
    BUFF_DISPEL(DispelType.class),
    //  驱散状态
    STATE_DISPEL(UnitState[].class),
    //  HP回复
    HP_RECOVER(HpRecoverParam.class),
    // 兴奋剂
    XING_FEN_JI(XingFenJiParam.class),
    // 兴奋剂，初始化移动
    XING_FEN_JI_FOLLOW,
    //  施放BUFF
    BUFF_CAST,
    // 将攻击被人造成的伤害，一部分平分给队友
    HUN_LI_JI_QU(HunLiJiQuParam.class),
    //  根据目标选择策略
    JUMP_CIRCLE_BY_TARGET_ID(JumpCircleByTargetIdParam.class),
    // 跳到敌人最密集的区域
    JUMP_CIRCLE(Integer.class),
    // 用餐时间
    AN_YE_JIANG_LIN(AnYeJiangLinParam.class),
    // 生成飞刀
    KNIFE,
    // 夜枭暗袭
    YE_XIAO_AN_XI(YeXiaoAnXiParam.class),
    // 暗影漩涡
    AN_YING_XUAN_WO(AnYingXuanWoParam.class),
    // 骨王之怒
    GU_WANG_ZHI_NU(GuWangZhiNuParam.class),
    //  复活
    REVIVE,
    //  组合效果
    COMBINATION,
    //  属性吸收
    VALUES_SUCK,
    // 嘲讽
    SNEER(StateAddParam.class),
    // 有护盾时额外造成法术伤害
    SHIELD_MAGIC_DAMAGE,
    // 有护盾时额外造成物理伤害
    SHIELD_PHYSIC_DAMAGE,
    // 影压
    YING_YA,
    // 灵魂收割效果
    SHI_GUI_ZHAO_HUAN(ShiGuiZhaoHuanParam.class),
    // 10次攻击后，必定暴击
    BAO_TOU,
    // 目标附近召唤单元
    TARGET_SUMMON_HP,
    // 暗影狂暴
    AN_YING_KUANG_BAO(AnYingKuangBaoParam.class),
    //雷云
    LEI_YUN,
    //召唤
    SUMMON(SummonSkillParam.class),
    //移动到目标身前
    MOVE_BEFORE(Integer.class),
    //移动到目标身后
    MOVE_BEHIND,
    //移动坐标轴对位
    MOVE_OPPOSITE,
    // 吸血
    SUCK(Double.class),
    // 走开走开
    ZOU_KAI_ZOU_KAI(Integer.class),
    // 魔穿
    MO_CHUAN,
    //添加一些属性/被动/buff
    INIT_ADD(InitAddParam.class),

    // 添加眩晕等状态
    STATE_ADD(StateAddParam.class),

    // 无畏旋风
    WU_WEI_XUAN_FENG(WuWeiXuanFengParam.class),
    // 英雄降临
    YING_XIONG_JIANG_LIN(YingXiongJiangLinParam.class),
    //添加共享被动
    COMMON_PASSIVE(String.class),
    // 若范围内只有1个敌军时，则将该敌军冰冻2秒
    STATE_ADD_BY_TARGET_AMOUNT(StateAddByTargetAmountParam.class),
    // 无情钩链：
    WU_QING_GOU_LIAN(WuQingGouLianParam.class),

    // 技能CD共享（不包含羁绊技能和初始化技能）
    SKILL_CD_SHARE(SkillCdShareParam.class),

    // 永恒守望·奥米茄专属装备
    YONG_HENG_SHOU_WANG_ZS(YongHengShouWangZSParam.class),
    // 大地咆哮·塔巴斯专属装备
    DA_DI_PAO_XIAO_ZS(DaDiPaoXiaoZSParam.class),
    // 深渊龙姬·安吉丽娜专属装备
    SHEN_YUAN_LONG_JI_ZS(ShenYuanLongJiZSParam.class),
    // 鸟嘴医生·查尔斯专属装备
    NIAO_ZUI_YI_SHENG_ZS(NiaoZuiYiShengZSParam.class),
    // 海妖公主·卡莉安娜专属装备
    HAI_YAO_GONG_ZHU_ZS(HaiYaoGongZhuZSParam.class),
    // 飞行技师·比佛利专属装备
    FEI_XING_JI_SHI_ZS(FeiXingJiShiZSParam.class),
    // 无尽杀戮的深渊
    @Deprecated
    WU_JIN_SHA_LU(WuJinShaLuParam.class),
    // 添加护盾，并与BUFF关联（如果身上没了护盾，将移除相关BUFF）
    ADD_SHIELD_AND_RELATE_BUFF(AddShieldAndRelateBuffParam.class),
    //使目标释放指定技能
    ADD_INIT_SKILL(String.class),
    // 屠杀序幕
    TU_SHA_XU_MU(TuShaXuMuParam.class),
    //斩风之息·武技能：斩在前
    ZHAN_ZAI_QIAN(ZhanZaiQianParam.class),
    //斩风之息·武技能：掠风斩命
    FENG_ZHI_XI_DAMAGE(FengZhiXiDamageParam.class),
    //陆海霸主·巴达克技能：火力支援
    HUO_LI_ZHI_YUAN(HuoLiZhiYuanParam.class),
    //陆海霸主·巴达克专属装备
    LU_HAI_BA_ZHU_ZS(LuHaiBaZhuZSParam.class),
    //风暴女皇·艾琳技能：暴风降临
    BAO_FENG_JIANG_LIN_DAMAGE(BaoFengJiangLinDamageParam.class),
    //风暴女皇·艾琳技能：风之链条
    FENG_ZHI_LIAN_TIAO(FengZhiLianTiaoParam.class),
    // 蝴蝶仙子·莉亚娜技能：迷蝶花海
    @Deprecated
    MI_DIE_HUA_HAI(MiDieHuaHaiParam.class),
    MI_DIE_HUA_HAI_REMAKE(MiDieHuaHaiParam.class),
    // 唤星女神·维纳斯技能：远星的呼唤
    YUAN_XING_DE_HU_HUAN(YuanXingDeHuHuanParam.class),
    // 唤星女神·维纳斯专属装备
    HUAN_XING_NV_SHEN_ZS(HuanXingNvShenZSParam.class),
    // 剧毒蛇影BOSS技能：剧毒漩涡
    JU_DU_XUAN_WO(JuDuXuanWoParam.class),
    // 维京战魂BOSS技能：画地为牢
    HUA_DI_WEI_LAO(HuaDiWeiLaoParam.class),
    // 巫蛊诡术
    WU_GU_GUI_SHU(WuGuGuiShuParam.class),

    ;

    private final Class<?> paramType;

    EffectType() {
        this.paramType = String.class;
    }

    EffectType(Class<?> paramType) {
        this.paramType = paramType;
    }

    public Class<?> getParamType() {
        return paramType;
    }
}

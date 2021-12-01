package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.alter.*;
import com.pangu.framework.protocol.annotation.Transable;

/**
 * 值修改类型
 */
@Transable
public enum AlterType {

	// 数值属性

	STRENGTH(new UnitValueAlter(UnitValue.STRENGTH)),

	AGILITY(new UnitValueAlter(UnitValue.AGILITY)),

	INTELLECT(new UnitValueAlter(UnitValue.INTELLECT)),

	HP(new HpAlter()),

	HP_MAX(new UnitValueAlter(UnitValue.HP_MAX)),

	LIFE(new LifeAlter()),

	HP_RECOVER(new UnitValueAlter(UnitValue.HP_RECOVER)),


	MP(new MpAlter()),
	MP_SUCK(new MpSuckAlter()),

	MP_MAX(new UnitValueAlter(UnitValue.MP_MAX)),

	// 怒气增加率
	RATE_MP_ADD_RATE(new RateAlter(UnitRate.MP_ADD_RATE)),


	RATE_NORMAL_MP_ADD(new RateAlter(UnitRate.NORMAL_MP_ADD)),

	RATE_KILL_MP_ADD(new RateAlter(UnitRate.KILL_MP_ADD)),

	RATE_DAMAGE_MP_ADD(new RateAlter(UnitRate.DAMAGE_MP_ADD)),


	NORMAL_MP_ADD(new UnitValueAlter(UnitValue.NORMAL_MP_ADD)),

	KILL_MP_ADD(new UnitValueAlter(UnitValue.KILL_MP_ADD)),

	DAMAGE_MP_ADD(new UnitValueAlter(UnitValue.DAMAGE_MP_ADD)),


	@Deprecated
	MP_ADD(new RateAlter(UnitRate.MP_ADD_RATE)),
	// 受伤怒气增加率
	@Deprecated
	RATE_BE_ATTACKED_MP(new RateAlter(UnitRate.DAMAGE_MP_ADD)),

	@Deprecated
	MP_RECOVER(new RateAlter(UnitRate.MP_ADD_RATE)),


	EP(new UnitValueAlter(UnitValue.EP)),


	SHIELD_SET(new ShieldUpdateAlter()),

	SHIELD_UPDATE(new ShieldUpdateAlter()),

	LIGHT_SHIELD(new UnitValueAlter(UnitValue.LIGHT_SHIELD)),

	ATTACK_P(new UnitValueAlter(UnitValue.ATTACK_P)),

	ATTACK_M(new UnitValueAlter(UnitValue.ATTACK_M)),

	DEFENCE_P(new UnitValueAlter(UnitValue.DEFENCE_P)),

	DEFENCE_M(new UnitValueAlter(UnitValue.DEFENCE_M)),


	PHYSICAL_PENETRATION(new UnitValueAlter(UnitValue.PHYSICAL_PENETRATION)),

	MAGIC_PENETRATION(new UnitValueAlter(UnitValue.MAGIC_PENETRATION)),

	REAL_DAMAGE(new UnitValueAlter(UnitValue.REAL_DAMAGE)),


	SPEED(new UnitValueAlter(UnitValue.SPEED)),

	// 比率属性(累加关系)
	//抗暴率
	RATE_UNCRIT(new RateAlter(UnitRate.UNCRIT)),
	//暴击率
	RATE_CRIT(new RateAlter(UnitRate.CRIT)),
	// 被暴击率(一般用于加到被攻击放，增加攻击方的暴击率)
	RATE_BE_CRIT(new RateAlter(UnitRate.BE_CRIT)),

	RATE_ATTACK_P_ADD(new RateAlter(UnitRate.ATTACK_P_ADD)),

	RATE_ATTACK_M_ADD(new RateAlter(UnitRate.ATTACK_M_ADD)),

	RATE_DEFENCE_P_ADD(new RateAlter(UnitRate.DEFENCE_P_ADD)),

	RATE_DEFENCE_M_ADD(new RateAlter(UnitRate.DEFENCE_M_ADD)),

	RATE_HP_MAX_ADD(new RateAlter(UnitRate.HP_MAX_ADD)),


	RATE_NORMAL_SKILL_UP(new RateAlter(UnitRate.NORMAL_SKILL_UP)),

	RATE_NORMAL_SKILL_DOWN(new RateAlter(UnitRate.NORMAL_SKILL_DOWN)),


	RATE_HIT(new RateAlter(UnitRate.HIT)),

	RATE_DODGY(new RateAlter(UnitRate.DODGY)),

	@Deprecated
	RATE_MP_DERATE(new RateAlter(UnitRate.MP_ADD_RATE)),
	@Deprecated
	RATE_NORMAL_MP_DERATE(new RateAlter(UnitRate.MP_ADD_RATE)),
	//暴击伤害增加
	RATE_CRIT_DAMAGE(new RateAlter(UnitRate.CRIT_DAMAGE)),
	//暴击伤害减免
	RATE_UNCRIT_DAMAGE(new RateAlter(UnitRate.UNCRIT_DAMAGE)),


	RATE_HURT_CRIT(new RateAlter(UnitRate.HURT_CRIT)),
	//增加技能cd
	RATE_SKILL_CD_UP(new RateAlter(UnitRate.SKILL_CD_UP)),
	//减少技能CD
	RATE_SKILL_CD_DOWN(new RateAlter(UnitRate.SKILL_CD_DOWN)),


	RATE_BLOCK(new RateAlter(UnitRate.BLOCK)),

	RATE_UNBLOCK(new RateAlter(UnitRate.UNBLOCK)),


	RATE_HARM_P(new RateAlter(UnitRate.HARM_P)),

	RATE_HARM_M(new RateAlter(UnitRate.HARM_M)),

	RATE_HARM(new HarmAlter()),

	RATE_UNHARM_P(new RateAlter(UnitRate.UNHARM_P)),

	RATE_UNHARM_M(new RateAlter(UnitRate.UNHARM_M)),

	RATE_UNHARM(new UnHarmAlter()),
	//吸血比率
	RATE_SUCK(new RateAlter(UnitRate.SUCK)),


	RATE_DISABLE(new RateAlter(UnitRate.DISABLE)),

	RATE_UNDISABLE(new RateAlter(UnitRate.UNDISABLE)),

	RATE_CHAOS(new RateAlter(UnitRate.CHAOS)),

	RATE_UNCHAOS(new RateAlter(UnitRate.UNCHAOS)),

	RATE_FUZZY(new RateAlter(UnitRate.FUZZY)),

	RATE_CURE(new RateAlter(UnitRate.CURE)),

	RATE_BCURE(new RateAlter(UnitRate.BCURE)),
	RATE_BCURE_DECREASE(new RateAlter(UnitRate.BCURE_DECREASE)),


	RATE_ARMOR_PENETRATION(new RateAlter(UnitRate.ARMOR_PENETRATION)),

	RATE_SPELL_PENETRATION(new RateAlter(UnitRate.SPELL_PENETRATION)),


	RATE_SPEED_UP(new RateAlter(UnitRate.SPEED_UP)),


	RATE_SPEED_DOWN(new RateAlter(UnitRate.SPEED_DOWN)),


	RATE_EXP(new RateAlter(UnitRate.EXP)),

	RATE_COPPER(new RateAlter(UnitRate.COPPER)),

	RATE_RECOVER_HP_TO_SHIELD(new RateAlter(UnitRate.RECOVER_HP_TO_SHIELD)),

	// 普通技能伤害加深
	RATE_NORMAL_SKILL_ADD(new RateAlter(UnitRate.NORMAL_SKILL_ADD)),
	// 技能伤害加深
	RATE_SKILL_ADD(new RateAlter(UnitRate.SKILL_ADD)),
	// 大招伤害加深
	RATE_SPACE_ADD(new RateAlter(UnitRate.SPACE_ADD)),


	RAE_BUFF_HARM_DEC(new RateAlter(UnitRate.BUFF_HARM_DEC)),


	RATE_STATE_HARM_DEC(new RateAlter(UnitRate.STATE_HARM_DEC)),


	RATE_PVE_ATTACK_DEC(new RateAlter(UnitRate.PVE_ATTACK_DEC)),


	RATE_PVP_ATTACK_ADD(new RateAlter(UnitRate.PVP_ATTACK_ADD)),


	RATE_CONTROL_STATE_IMMUNE(new RateAlter(UnitRate.CONTROL_STATE_IMMUNE_RATE)),


	RATE_MALE_DEEPEN(new RateAlter(UnitRate.MALE_DEEPEN)),


	RATE_FEMALE_DEEPEN(new RateAlter(UnitRate.MALE_DEEPEN)),
	;


	AlterType(Alter alter) {
		this.alter = alter;
	}


	private final Alter alter;

	public Alter getAlter() {
		return alter;
	}

	public Object toValue(String value) {
		return alter.toValue(value);
	}

}

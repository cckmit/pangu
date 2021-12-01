package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战斗单位的数值属性
 */
@Transable
public enum UnitValue {


	STRENGTH,

	AGILITY,

	INTELLECT,

	HP,

	HP_MAX,

	HP_RECOVER,

	TOUGHNESS,
	

	MP,

	MP_MAX,


	EP,
	EP_MAX,


	NORMAL_MP_ADD,

	KILL_MP_ADD,

	DAMAGE_MP_ADD,
	

	SHIELD,

	LIGHT_SHIELD,

	ATTACK_P,

	ATTACK_M,

	DEFENCE_P,

	DEFENCE_M,
	

	PHYSICAL_PENETRATION,

	MAGIC_PENETRATION,

	REAL_DAMAGE,
	

	SPEED,


	CURE_VALUE,

	BE_CURE_VALUE,
	;
}

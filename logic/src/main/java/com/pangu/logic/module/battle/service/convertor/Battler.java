package com.pangu.logic.module.battle.service.convertor;

import com.pangu.logic.module.battle.model.FighterType;

/**
 * 作战标识<br/>
 * 通过该对象来标识，战斗计算的攻击方或防守方
 */
public final class Battler<T> {

	//  战斗单位类型 
	private final FighterType type;
	//  战斗单位标识信息 
	private final T content;

	//  构造方法 
	private Battler(FighterType type, T content) {
		this.type = type;
		this.content = content;
	}

	/**
	 * 获取战斗单元类型描述
	 * @return
	 */
	public FighterType getType() {
		return type;
	}

	/**
	 * 获取战斗单元标识
	 * @return
	 */
	public T getContent() {
		return content;
	}

	// Static Method's ...

	//  便捷构造方法 
	public static <T> Battler<T> valueOf(FighterType type, T id) {
		return new Battler<T>(type, id);
	}

}

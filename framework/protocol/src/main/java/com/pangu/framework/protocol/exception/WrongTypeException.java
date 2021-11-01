package com.pangu.framework.protocol.exception;

/**
 * 错误的代理类型
 * @author Ramon
 */
public class WrongTypeException extends RuntimeException {
	private static final long serialVersionUID = 4014279564958034497L;

	/**
	 * 未知类型
	 * @param type
	 */
	public WrongTypeException(int type) {
		super("未定义代理类型[" + Integer.toHexString(type) + "]");
	}

	public WrongTypeException(int need, int except) {
		super("代理类型[" + Integer.toHexString(need) + "]与当前类型[" + Integer.toHexString(except) + "]不匹配");
	}
}

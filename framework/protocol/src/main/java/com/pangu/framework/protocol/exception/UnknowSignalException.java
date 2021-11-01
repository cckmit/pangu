package com.pangu.framework.protocol.exception;

/**
 * 错误的值标志
 * 
 * @author Ramon
 */
public class UnknowSignalException extends RuntimeException{
	private static final long serialVersionUID = -7844776109254208895L;

	public UnknowSignalException(int type, int signal) {
		super("代理类型[" + type + "]无法识别的值标记[" + signal + "]");
	}
}

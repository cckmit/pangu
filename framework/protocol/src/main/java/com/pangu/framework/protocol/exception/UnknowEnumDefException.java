package com.pangu.framework.protocol.exception;

/**
 * 未定义的传输枚举类型
 * 
 * @author author
 *
 */
public class UnknowEnumDefException extends RuntimeException{
	private static final long serialVersionUID = 4895297111772939299L;

	public UnknowEnumDefException(int rawType) {
		super("未定义的传输枚举类型[" + rawType + "]");
	}

	public UnknowEnumDefException(Class<? extends Object> clz) {
		super("未定义的传输枚举类型[" + clz.getName() + "]");
	}
	
}

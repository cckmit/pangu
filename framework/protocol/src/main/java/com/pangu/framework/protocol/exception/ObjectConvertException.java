package com.pangu.framework.protocol.exception;

/**
 * 类型转换异常
 * 
 * @author author
 *
 */
public class ObjectConvertException extends RuntimeException{
	private static final long serialVersionUID = -671045745597774362L;

	public ObjectConvertException(Exception e) {
		super("类型转换异常", e);
	}

}

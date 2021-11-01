package com.pangu.framework.protocol.exception;

/**
 * 类型代理异常
 * @author Ramon
 */
public class ObjectProxyException extends RuntimeException {
	private static final long serialVersionUID = -671045745597774362L;

	public ObjectProxyException(String message, Exception e) {
		super("类型代理异常 - " + message, e);
	}

}

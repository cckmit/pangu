package com.pangu.framework.utils;

import org.slf4j.helpers.MessageFormatter;

import com.pangu.framework.utils.model.Result;

/**
 * 受管理异常
 * @author author
 */
public class ManagedException extends RuntimeException {

	private static final long serialVersionUID = -5566075318388205571L;

	/** 错误代码 */
	private final int code;

	ManagedException(String msg) {
		super(msg);
		this.code = Result.UNKNOW_ERROR;
	}
	ManagedException(Throwable cause) {
		super(cause);
		this.code = Result.UNKNOW_ERROR;
	}

	public ManagedException(int code) {
		super(MessageFormatter.format("受管理异常 : [{}]", code).getMessage());
		this.code = code;
	}

	public ManagedException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public ManagedException(int code, String message) {
		super(message);
		this.code = code;
	}

	public ManagedException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	/**
	 * 获取错误代码
	 * @return
	 */
	public int getCode() {
		return code;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}

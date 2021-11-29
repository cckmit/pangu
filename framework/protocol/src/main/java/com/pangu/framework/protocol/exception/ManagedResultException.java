package com.pangu.framework.protocol.exception;

import com.pangu.framework.utils.ManagedException;
import com.pangu.framework.utils.model.Result;

/**
 * 转换结果对象的受控异常
 * @author author
 */
public class ManagedResultException extends ManagedException {
	private static final long serialVersionUID = 4138844494118698057L;

	private final Result<?> content;

	public Result<?> getContent() {
		return content;
	}

	public ManagedResultException(int code, Result<?> content) {
		super(code);
		this.content = content;
	}
}

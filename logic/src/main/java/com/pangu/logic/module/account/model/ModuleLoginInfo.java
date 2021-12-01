package com.pangu.logic.module.account.model;

public class ModuleLoginInfo {
	
	private short module;

	
	private Object content;

	public short getModule() {
		return module;
	}

	public void setModule(byte module) {
		this.module = module;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * 构造方法
	 * @param module 模块号
	 * @param content 模块登录信息vo
	 * @return
	 */
	public static ModuleLoginInfo valueOf(short module, Object content) {
		ModuleLoginInfo message = new ModuleLoginInfo();
		message.module = module;
		message.content = content;
		return message;
	}
}

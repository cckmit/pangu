package com.pangu.framework.console;

import com.pangu.framework.console.exception.CommandException;

public interface Command {

	/**
	 * 获取命令名
	 * @return
	 */
	String name();
	
	/**
	 * 获取命令的描述信息
	 * @return
	 */
	String description();
	
	/**
	 * 执行控制台指令
	 * @param arguments 命令参数
	 * @throws CommandException 执行失败时抛出
	 */
	void execute(String[] arguments) throws CommandException;
}

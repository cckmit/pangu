package com.pangu.framework.console;

import org.apache.commons.lang3.StringUtils;

public class CommandHelper {
	
	
	public static final String SPLIT = " ";

	/**
	 * 获取控制台指令名
	 * @param line 控制台输入内容
	 * @return 如果无法获取将返回null
	 */
	public static String getName(String line) {
		if (StringUtils.isBlank(line)) {
			return null;
		}
		
		String[] arguments = StringUtils.split(line);
		if (arguments.length == 0) {
			return null;
		}
		return arguments[0];
	}

	/**
	 * 获取控制台指令参数
	 * @param line 控制台输入内容
	 * @return 如果无法获取将返回0长数组，不会返回null
	 */
	public static String[] getArguments(String line) {
		if (StringUtils.isBlank(line)) {
			return new String[0];
		}
		String[] arguments = StringUtils.split(line);
		if (arguments.length <= 1) {
			return new String[0];
		}
		String[] result = new String[arguments.length - 1];
		System.arraycopy(arguments, 1, result, 0, arguments.length - 1);
		return result;
	}

}

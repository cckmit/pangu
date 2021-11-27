package com.pangu.framework.console.impl;

import java.lang.reflect.Method;

import com.pangu.framework.console.Command;
import com.pangu.framework.console.ConsoleCommand;
import com.pangu.framework.console.exception.ArgumentException;
import com.pangu.framework.console.exception.CommandException;
import com.pangu.framework.console.exception.ExecuteException;
import org.springframework.core.convert.ConversionService;

public class MethodCommand implements Command {

	private final String name;
	private final String description;
	private final Object target;
	private final Method method;
	private final Class<?>[] types;
	private final ConversionService conversionService;

	public MethodCommand(Object target, Method method, ConversionService conversionService) {
		this.target = target;
		this.conversionService = conversionService;

		method.setAccessible(true);
		this.method = method;

		ConsoleCommand annotation = method.getAnnotation(ConsoleCommand.class);
		this.name = annotation.name().trim();
		this.description = annotation.description().trim();
		this.types = method.getParameterTypes();
	}

	@Override
	public void execute(String[] arguments) throws CommandException {
		if (arguments.length != types.length) {
			throw new ArgumentException("指令参数长度不正确");
		}

		Object[] args = new Object[arguments.length];
		try {
			for (int i = 0; i < arguments.length; i++) {
				if (arguments[i].equals("null")) {
					args[i] = null;
				} else {
					args[i] = conversionService.convert(arguments[i], types[i]);
				}
			}
		} catch (Exception e) {
			throw new ArgumentException("参数转换时出现异常 - " + e.getMessage(), e);
		}

		try {
			method.invoke(target, args);
		} catch (Exception e) {
			throw new ExecuteException("方法执行时出现异常 - " + e.getMessage(), e);
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}
}

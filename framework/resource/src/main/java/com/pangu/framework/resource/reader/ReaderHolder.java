package com.pangu.framework.resource.reader;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.pangu.framework.resource.other.FormatDefinition;

public class ReaderHolder implements ApplicationContextAware {
	
	public final static String FORMAT_SETTER = "format";

	/** 格式定义信息 */
	private FormatDefinition format;
	private final ConcurrentHashMap<String, ResourceReader> readers = new ConcurrentHashMap<String, ResourceReader>();

	@PostConstruct
	protected void initialize() {
		for (String name : this.applicationContext.getBeanNamesForType(ResourceReader.class)) {
			ResourceReader reader = this.applicationContext.getBean(name, ResourceReader.class);
			this.register(reader);
		}
	}

	/**
	 * 获取指定格式的 {@link ResourceReader}
	 * @param format
	 * @return
	 */
	public ResourceReader getReader(String format) {
		return readers.get(format);
	}

	/**
	 * 注册指定的 {@link ResourceReader}
	 * @param reader
	 * @return
	 */
	public ResourceReader register(ResourceReader reader) {
		if (reader.getFormat().equals(format.getType()) && format.getConfig() != null) {
			reader.config(format.getConfig());
		}
		return readers.putIfAbsent(reader.getFormat(), reader);
	}

	// 实现 {@link ApplicationContextAware}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setFormat(FormatDefinition format) {
		this.format = format;
	}

}

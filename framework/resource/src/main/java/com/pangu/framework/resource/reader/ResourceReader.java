package com.pangu.framework.resource.reader;

import java.io.InputStream;
import java.util.List;

/**
 * 资源读取接口
 * @author frank
 */
public interface ResourceReader {
	
	/**
	 * 配置方法
	 * @param config 配置信息
	 */
	void config(String config);
	
	/**
	 * 获取处理资源的格式名
	 * @return
	 */
	String getFormat();
	
	/**
	 * 从输入流读取资源实例
	 * @param <E>
	 * @param input 输入流
	 * @param clz 资源实例类型
	 * @return
	 */
	<E> List<E> read(InputStream input, Class<E> clz);
}

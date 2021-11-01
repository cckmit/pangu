package com.pangu.framework.utils.id;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多服的主键生成器
 * @author frank
 */
public class MultiServerIdGenerator {

	/** 主键生成器映射 */
	private ConcurrentMap<Integer, IdGenerator> generators = new ConcurrentHashMap<Integer, IdGenerator>();

	/**
	 * 主键生成器映射键值
	 */
	private int getKey(int operator, int server) {
		return (operator << 16) + server;
	}

	public boolean isInit(int operator, int server) {
		int key = getKey(operator, server);
		return generators.containsKey(key);
	}

	/**
	 * 添加指定服标识的主键生成器
	 * @param server 服标识
	 * @param max 当前的主键最大值
	 */
	public void init(int operator, int server, Long max) {
		int key = getKey(operator, server);
		if (generators.containsKey(key)) {
			throw new IllegalStateException("运营商[" + operator + "]服务器[" + server + "]的主键生成器已存在");
		}
		IdGenerator generator = new IdGenerator(operator, server, max);
		generators.putIfAbsent(key, generator);
	}

	/**
	 * 获取下一个自增主键
	 * @param server 服标识
	 * @return
	 */
	public long getNext(int operator, int server) {
		int key = getKey(operator, server);
		IdGenerator generator = generators.get(key);
		if (generator == null) {
			throw new IllegalStateException("运营商[" + operator + "]服务器[" + server + "]的主键生成器不存在");
		}
		return generator.getNext();
	}
}

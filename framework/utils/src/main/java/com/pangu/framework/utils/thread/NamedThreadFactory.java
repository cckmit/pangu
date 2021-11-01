package com.pangu.framework.utils.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可命名线程组
 * @author frank
 */
public class NamedThreadFactory implements ThreadFactory {

	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public NamedThreadFactory(ThreadGroup group, String name) {
		this.group = group;
		this.namePrefix = group.getName() + ":" + name;
	}

	public NamedThreadFactory(String name) {
		this.group = new ThreadGroup(name);
		this.namePrefix = group.getName() + ":";
	}

	public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
		return thread;
	}

}

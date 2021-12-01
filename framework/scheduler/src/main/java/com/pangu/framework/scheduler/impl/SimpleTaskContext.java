package com.pangu.framework.scheduler.impl;

import java.util.Date;

import com.pangu.framework.scheduler.TaskContext;

/**
 * 简单的任务上下文对象
 * @author author
 */
public class SimpleTaskContext implements TaskContext {


	private volatile Date lastScheduledTime;

	private volatile Date lastActualTime;

	private volatile Date lastCompletionTime;

	public void update(Date scheduledTime, Date actualTime, Date completionTime) {
		this.lastScheduledTime = scheduledTime;
		this.lastActualTime = actualTime;
		this.lastCompletionTime = completionTime;
	}

	public Date lastScheduledTime() {
		return this.lastScheduledTime;
	}

	public Date lastActualTime() {
		return this.lastActualTime;
	}

	public Date lastCompletionTime() {
		return this.lastCompletionTime;
	}

}

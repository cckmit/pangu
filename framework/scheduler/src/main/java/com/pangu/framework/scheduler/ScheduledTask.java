package com.pangu.framework.scheduler;

/**
 * 定时任务接口
 * @author author
 */
public interface ScheduledTask extends Runnable {

	/**
	 * 获取当前任务的任务名
	 * @return
	 */
	String getName();
}

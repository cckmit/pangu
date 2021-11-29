package com.pangu.framework.scheduler.impl;

import java.util.Date;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.pangu.framework.scheduler.ScheduledTask;
import com.pangu.framework.scheduler.Scheduler;
import com.pangu.framework.scheduler.Trigger;
import com.pangu.framework.utils.thread.NamedThreadFactory;

/**
 * 定时任务调度器
 * @author author
 */
@Component
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SimpleScheduler implements Scheduler, ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(SimpleScheduler.class);

	@Autowired(required = false)
	@Qualifier("scheduling_delay_time")
	private final Long delayTime = 60000L;
	@Autowired(required = false)
	@Qualifier("scheduling_pool_size")
	private final Integer poolSize = 5;

	private final AtomicBoolean running = new AtomicBoolean();

	private FixScheduledThreadPoolExecutor executor;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (!running.compareAndSet(false, true)) {
			return;
		}
		// 启动执行线程
		executor.startUp();
		if (logger.isInfoEnabled()) {
			logger.info("启动定时器执行线程");
		}
	}

	@PostConstruct
	protected void init() {
		if (logger.isInfoEnabled()) {
			logger.info("定时任务线程池大小:{}，修正时间延迟:{}", poolSize, delayTime);
		}
		ThreadGroup group = new ThreadGroup("定时任务");
		NamedThreadFactory threadFactory = new NamedThreadFactory(group, "处理");
		executor = new FixScheduledThreadPoolExecutor(poolSize, delayTime, threadFactory, false);
	}

	@PreDestroy
	protected void destory() {
		if (executor != null) {
			executor.shutdownNow();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("定时任务调度器已经关闭");
		}
	}

	/**
	 * 定时任务池大小
	 */
	public int getSchedulerQueueSize() {
		return executor.getQueue().size();
	}

	/**
	 * 池正在执行的线程数
	 */
	public int getPoolActiveCount() {
		return executor.getActiveCount();
	}

	@Override
	public ScheduledFuture schedule(ScheduledTask task, Trigger trigger) {
		try {
			task = new LogDecorateTask(task);
			return new SchedulingRunner(task, trigger, this.executor).schedule();
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("执行器不接受[" + task.getName() + "]该任务", ex);
		}
	}

	@Override
	public ScheduledFuture schedule(ScheduledTask task, Date startTime) {
		long initialDelay = startTime.getTime() - System.currentTimeMillis();
		try {
			task = new LogDecorateTask(task);
			return this.executor.schedule(task, initialDelay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("执行器不接受[" + task.getName() + "]该任务", ex);
		}
	}

	@Override
	public ScheduledFuture<?> schedule(ScheduledTask task, String cron) {
		if (StringUtils.isBlank(cron)) {
			// 不执行空CRON
			return null;
		}
		CronTrigger trigger = new CronTrigger(cron);
		return schedule(task, trigger);
	}

	@Override
	public ScheduledFuture scheduleAtFixedRate(ScheduledTask task, Date startTime, long period) {
		long initialDelay = startTime.getTime() - System.currentTimeMillis();
		try {
			task = new LogDecorateTask(task);
			return this.executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("执行器不接受[" + task.getName() + "]该任务", ex);
		}
	}

	@Override
	public ScheduledFuture scheduleAtFixedRate(ScheduledTask task, long period) {
		try {
			task = new LogDecorateTask(task);
			return this.executor.scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("执行器不接受[" + task.getName() + "]该任务", ex);
		}
	}

	@Override
	public ScheduledFuture scheduleWithFixedDelay(ScheduledTask task, Date startTime, long delay) {
		long initialDelay = startTime.getTime() - System.currentTimeMillis();
		try {
			task = new LogDecorateTask(task);
			return this.executor.scheduleWithFixedDelay(task, initialDelay, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("执行器不接受[" + task.getName() + "]该任务", ex);
		}
	}

	@Override
	public ScheduledFuture scheduleWithDelay(ScheduledTask task, long delay) {
		try {
			task = new LogDecorateTask(task);
			return this.executor.schedule(task, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("执行器不接受[" + task.getName() + "]该任务", ex);
		}
	}

	/**
	 * 用于做日志记录的任务装饰类
	 * @author author
	 */
	private static class LogDecorateTask implements ScheduledTask {

		private final ScheduledTask task;

		public LogDecorateTask(ScheduledTask task) {
			this.task = task;
		}

		public String getName() {
			return task.getName();
		}

		public void run() {
			if (logger.isDebugEnabled()) {
				logger.debug("任务[{}]开始运行时间[{}]", task.getName(), new Date());
			}
			try {
				task.run();
			} catch (RuntimeException e) {
				logger.error("任务[" + task.getName() + "]在执行时出现异常!", e);
				throw e;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("任务[{}]结束运行时间[{}]", task.getName(), new Date());
			}
		}

	}

}

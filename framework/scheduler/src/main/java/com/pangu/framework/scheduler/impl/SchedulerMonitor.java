package com.pangu.framework.scheduler.impl;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class SchedulerMonitor implements SchedulerMonitorMBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final AtomicInteger INSTANCE = new AtomicInteger();
	
	@Autowired
	private SimpleScheduler scheduler;

	private ObjectName jmxName;

	@PostConstruct
	protected void init() {
		// 注册MBean
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			jmxName = new ObjectName("com.pangu.framework:type=SchedulerMBean" + INSTANCE.incrementAndGet());
			mbs.registerMBean(this, jmxName);
		} catch (Exception e) {
			logger.error("JMX", e);
		}
	}

	@PreDestroy
	void destroy(){
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(jmxName);
		} catch (InstanceNotFoundException | MBeanRegistrationException e) {
			logger.error("unregister JMX", e);
		}
	}
	
	@Override
	public int getSchedulerQueueSize() {
		return scheduler.getSchedulerQueueSize();
	}

	@Override
	public int getPoolActiveCount() {
		return scheduler.getPoolActiveCount();
	}

}

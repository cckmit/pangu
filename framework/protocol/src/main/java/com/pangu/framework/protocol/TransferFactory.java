package com.pangu.framework.protocol;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TransferFactory implements FactoryBean<Transfer>, ApplicationContextAware {

	/** 资源定义列表 */
	private List<IndexedClass> transables;

	private Transfer transfer;

	// GETTER / SETTER

	public List<IndexedClass> getTransables() {
		return transables;
	}

	public void setTransables(List<IndexedClass> transables) {
		this.transables = transables;
	}

	// 实现接口的方法

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
		transfer = beanFactory.createBean(Transfer.class);
		List<IndexedClass> list = getTransables();
		Collections.sort(list);
		for (IndexedClass clz : list) {
			transfer.register(clz.getClz(), clz.getIdx());
		}
	}

	@Override
	public Transfer getObject() throws Exception {
		return transfer;
	}

	@Override
	public Class<Transfer> getObjectType() {
		return Transfer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}

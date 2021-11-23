package com.pangu.framework.protocol;

import org.springframework.beans.factory.FactoryBean;

import java.util.Collections;
import java.util.List;

public class TransferFactory implements FactoryBean<Transfer> {

	/** 资源定义列表 */
	private List<IndexedClass> transables;

	private Transfer transfer;

	// GETTER / SETTER
	private boolean server;
	public List<IndexedClass> getTransables() {
		return transables;
	}

	public void setTransables(List<IndexedClass> transables) {
		this.transables = transables;
	}

	@Override
	public Transfer getObject() {
		if (transfer == null) {
			transfer = new Transfer();
			List<IndexedClass> list = getTransables();
			Collections.sort(list);
			for (IndexedClass clz : list) {
				transfer.register(clz.getClz(), clz.getIdx());
			}
		}
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

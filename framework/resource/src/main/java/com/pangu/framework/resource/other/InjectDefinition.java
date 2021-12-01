package com.pangu.framework.resource.other;

import java.lang.reflect.Field;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.pangu.framework.resource.anno.InjectBean;

public class InjectDefinition {


	private final Field field;

	private final InjectBean inject;

	private final InjectType type;
	
	public InjectDefinition(Field field) {
		if (field == null) {
			throw new IllegalArgumentException("被注入属性域不能为null");
		}
		if (!field.isAnnotationPresent(InjectBean.class)) {
			throw new IllegalArgumentException("被注入属性域" + field.getName() + "的注释配置缺失");
		}
		field.setAccessible(true);
		
		this.field = field;
		this.inject = field.getAnnotation(InjectBean.class);
		if (StringUtils.isEmpty(this.inject.value())) {
			this.type = InjectType.CLASS;
		} else {
			this.type = InjectType.NAME;
		}
	}
	
	/**
	 * 获取注入值
	 * @param applicationContext
	 * @return
	 */
	public Object getValue(ApplicationContext applicationContext) {
		if (InjectType.NAME.equals(type)) {
			String value = inject.value();
			boolean containsBean = applicationContext.containsBean(value);
			if (!containsBean) {
				return null;
			}
			return applicationContext.getBean(value);
		} else {
			Class<?> type = field.getType();
			String[] beanNamesForType = applicationContext.getBeanNamesForType(type);
			if (ArrayUtils.isEmpty(beanNamesForType)) {
				return null;
			}
			return applicationContext.getBean(type);
		}
	}

	// Getter and Setter ...

	public InjectType getType() {
		return type;
	}

	public Field getField() {
		return field;
	}

	public InjectBean getInject() {
		return inject;
	}

}

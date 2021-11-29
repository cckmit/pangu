package com.pangu.framework.resource.schema;

import java.lang.reflect.Field;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;

/**
 * 静态资源变更观察者
 * @author author
 */
@SuppressWarnings("rawtypes")
public class StaticObserver implements Observer {

	private final static Logger logger = LoggerFactory.getLogger(StaticObserver.class);

	/** 接收更新通知 */
	@Override
	public void update(Observable o, Object arg) {
		if (!(o instanceof Storage)) {
			if (logger.isDebugEnabled()) {
				FormattingTuple message = MessageFormatter.format("被观察对象[{}]不是指定类型", o.getClass());
				logger.debug(message.getMessage());
			}
			return;
		}

		inject((Storage) o);
	}

	/** 注入资源实例 */
	private void inject(Storage o) {
		@SuppressWarnings("unchecked")
		final Object value = o.get(key, false);
		if (anno.required() && value == null) {
			FormattingTuple message = MessageFormatter.format("被注入属性[{}]不存在[key:{}]", field, key);
			logger.error(message.getMessage());
			throw new RuntimeException(message.getMessage());
		}

		Class<?> fieldClz = field.getType();
		if (value == null) {
			// NULL不注入
		} else if (fieldClz.isInstance(value)) {
			// 注入实例
			inject(value);
		} else {
			// 注入属性
			final String name = anno.field();
			if (StringUtils.isEmpty(name)) {
				FormattingTuple message = MessageFormatter.format("属性[{}]的注入的字段属性未定义", field);
				logger.debug(message.getMessage());
				throw new RuntimeException(message.getMessage());
			}

			ReflectionUtils.doWithFields(clz, new FieldCallback() {
				@Override
				public void doWith(Field subField) throws IllegalArgumentException, IllegalAccessException {
					if (subField.getName().equals(name)) {
						ReflectionUtils.makeAccessible(subField);
						Object subValue = subField.get(value);
						if (subValue == null) {
							return;
						}
						if (!field.getType().isInstance(subValue)) {
							try {
								subValue = conversionService.convert(subValue,
										TypeDescriptor.valueOf(subValue.getClass()), new TypeDescriptor(field));
							} catch (Exception e) {
								FormattingTuple message = MessageFormatter.format("属性[{}]的类型转换异常", field);
								logger.debug(message.getMessage());
								throw new RuntimeException(message.getMessage(), e);
							}
						}
						inject(subValue);
					}
				}
			});
		}
	}

	protected void inject(Object value) {
		try {
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			FormattingTuple message = MessageFormatter.format("无法设置被注入属性[{}]", field);
			logger.error(message.getMessage());
			throw new RuntimeException(message.getMessage(), e);
		}
	}

	/** 注入目标 */
	private final Object target;
	/** 注入属性 */
	private final Field field;
	/** 注入属性 */
	private final Static anno;
	/** 资源键值 */
	private final Object key;
	/** 资源键值 */
	private final Class<?> clz;
	/** 转换服务 */
	private final ConversionService conversionService;

	public StaticObserver(ConversionService conversionService, Object target, Field field, Static anno, Object key,
			Class<?> clz) {
		this.target = target;
		this.field = field;
		this.anno = anno;
		this.key = key;
		this.clz = clz;
		this.conversionService = conversionService;
	}

	// Getter and Setter ...

	public Object getTarget() {
		return target;
	}

	public Field getField() {
		return field;
	}

	public Static getAnno() {
		return anno;
	}

	public Object getKey() {
		return key;
	}

	public Class<?> getClz() {
		return clz;
	}

}

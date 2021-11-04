package com.pangu.framework.resource.support;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import com.pangu.framework.utils.json.JsonUtils;

/**
 * 将json格式的array字符串转换成对应的数组实例
 * @author frank
 */
public class JsonToArrayConverter implements ConditionalGenericConverter {

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.getType() != String.class) {
			return false;
		}
		if (!targetType.getType().isArray()) {
			return false;
		}
		return true;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Object[].class));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		String content = (String) source;
		TypeDescriptor elementType = targetType.getElementTypeDescriptor();
		Class<?> type = elementType.getType();
		if (!content.startsWith("[")) {
			Object array = Array.newInstance(type, 1);
			Object value;
			if (String.class.isAssignableFrom(type)) {
				value = content;
			} else {
				value = JsonUtils.string2Object(content, type);
			}
			Array.set(array, 0, value);
			return array;
		}

		if (elementType.isPrimitive()) {
			return JsonUtils.string2Object(content, targetType.getObjectType());
		} else {
			return JsonUtils.string2Array(content, elementType.getType());
		}
	}

}

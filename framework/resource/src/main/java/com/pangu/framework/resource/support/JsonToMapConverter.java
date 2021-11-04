package com.pangu.framework.resource.support;

import com.pangu.framework.utils.json.JsonUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 将json格式的map字符串转换成对应的Map实例
 *
 * @author frank
 */
public class JsonToMapConverter implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType() != String.class) {
            return false;
        }
        if (!Map.class.isAssignableFrom(targetType.getType())) {
            return false;
        }
        return true;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Map.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        String string = (String) source;
        Class type = targetType.getType();
        final ResolvableType[] generics = targetType.getResolvableType().getGenerics();
        return JsonUtils.string2Map(string, generics[0].getType(), generics[1].getType(), type);
    }

}

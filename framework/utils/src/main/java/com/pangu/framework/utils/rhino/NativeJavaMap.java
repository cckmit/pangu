package com.pangu.framework.utils.rhino;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

/**
 * Map 对象参数包装类
 * @author author
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NativeJavaMap extends NativeJavaObject {
	private static final long serialVersionUID = 3960951059804772962L;

	/**
	 * Map的泛型类型
	 */
	private Type[] kvType;
	
	public NativeJavaMap(Scriptable scope, Object javaObject) {
		super(scope, javaObject, ScriptRuntime.ObjectClass);
	}

	@Override
	public String getClassName() {
		return "JavaMap";
	}

	private Map getValuesMap() {
		return (Map) javaObject;
	}

	public static NativeJavaMap wrap(Map obj, Scriptable scope) {
		NativeJavaMap map = new NativeJavaMap(scope, obj);
		return map;
	}

	/**
	 * 获取Map的<键, 值>泛型类型
	 * @return
	 */
	private Type[] getKVType() {
		if (kvType == null) {
			Type[] genericInterfaces = getValuesMap().getClass().getGenericInterfaces();
			for (Type t : genericInterfaces) {
				if (t instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) t;
					Type rawType = pt.getRawType();
					if (rawType.equals(Map.class)) {
						Type[] args = pt.getActualTypeArguments();
						kvType = args;
						break;
					}
				}
			}
			kvType = new Type[] { Object.class, Object.class };
		}
		return kvType;
	}

	/**
	 * 检测是否合法Map的键类型
	 * @param <T>
	 * @param clz
	 * @return
	 */
	private <T> boolean isKeyType(Class<T> clz) {
		Type[] kvType = this.getKVType();
		return kvType[0] == Object.class || kvType[0] == clz || ((Class) kvType[0]).isAssignableFrom(clz);
	}

	@Override
	public Object get(String name, Scriptable start) {
		if (has(name, start)) {
			return getValuesMap().get(name);
		}
		return super.get(name, start);
	}

	@Override
	public Object get(int index, Scriptable start) {
		if (has(index, start)) {
			return getValuesMap().get(index);
		}
		return super.get(index, start);
	}

	@Override
	public boolean has(String name, Scriptable start) {
		if (isKeyType(String.class)) {
			return getValuesMap().containsKey(name);
		}
		return super.has(name, start);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		if (isKeyType(String.class)) {
			return getValuesMap().containsKey(index);
		}
		return super.has(index, start);
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		if (isKeyType(String.class)) {
			getValuesMap().put(name, value);
			return;
		}
		super.put(name, start, value);
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		if (isKeyType(Integer.class)) {
			getValuesMap().put(index, value);
			return;
		}
		super.put(index, start, value);
	}

	@Override
	public void delete(String name) {
		if (isKeyType(String.class)) {
			getValuesMap().remove(name);
			return;
		}
		super.delete(name);
	}

	@Override
	public void delete(int index) {
		if (isKeyType(Integer.class)) {
			getValuesMap().remove(index);
			return;
		}
		super.delete(index);
	}
}

package com.pangu.framework.utils.rhino;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

/**
 * Number 对象参数包装类
 * @author author
 */
public class NativeJavaNumber extends NativeJavaObject {
	private static final long serialVersionUID = 3960951059804772962L;

	@Override
	public String getClassName() {
		return "Number";
	}

	public NativeJavaNumber(Scriptable scope, Object javaObject) {
		super(scope, javaObject, javaObject.getClass());
	}

	public static NativeJavaNumber wrap(Number obj, Scriptable scope) {
		NativeJavaNumber map = new NativeJavaNumber(scope, obj);
		return map;
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		Object value;
		if (hint == ScriptRuntime.StringClass) {
			value = javaObject.toString();
		} else {
			return ((Number) javaObject).doubleValue();
		}
		return value;
	}

}

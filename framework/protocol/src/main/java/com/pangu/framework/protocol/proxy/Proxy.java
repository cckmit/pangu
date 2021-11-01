package com.pangu.framework.protocol.proxy;

import com.pangu.framework.protocol.Context;

public interface Proxy<T> {

	T getValue(Context ctx, byte flag);

	void setValue(Context ctx, T value);

}
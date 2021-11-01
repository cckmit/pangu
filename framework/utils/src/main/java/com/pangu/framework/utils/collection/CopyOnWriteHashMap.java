package com.pangu.framework.utils.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 线程安全的map，主要用在实体中（参考mina的CopyOnWriteMap） 不用ConcurrentHashMap的原因：占用空间太大 不用读写锁的原因： 实体中的map一般都很小，复制很快 而且读多写少，读的时候不加读锁
 * 效率更高 注意： 如果是set 请用CopyOnWriteArraySet 如果是list 请用CopyOnWriteArrayList
 */
public class CopyOnWriteHashMap<K, V> implements Map<K, V> {
	private volatile Map<K, V> internalMap;

	public CopyOnWriteHashMap() {
		internalMap = new HashMap<K, V>();
	}

	public CopyOnWriteHashMap(Map<K, V> values) {
		internalMap = new HashMap<K, V>(values);
	}

	// === 修改的方法 需要加synchronized

	public synchronized boolean replace(K key, V oldValue, V newValue) {
		V checkValue = internalMap.get(key);
		if (checkValue == null || checkValue.equals(oldValue)) {
			Map<K, V> newMap = new HashMap<K, V>(internalMap);
			newMap.put(key, newValue);
			internalMap = newMap;
			return true;
		}
		return false;
	}

	public synchronized V put(K key, V value) {
		Map<K, V> newMap = new HashMap<K, V>(internalMap);
		V val = newMap.put(key, value);
		internalMap = newMap;
		return val;
	}

	public synchronized void putAll(Map<? extends K, ? extends V> newData) {
		Map<K, V> newMap = new HashMap<K, V>(internalMap);
		newMap.putAll(newData);
		internalMap = newMap;
	}

	@Override
	public synchronized V remove(Object key) {
		Map<K, V> newMap = new HashMap<K, V>(internalMap);
		V val = newMap.remove(key);
		internalMap = newMap;
		return val;
	}

	@Override
	public synchronized void clear() {
		internalMap = new HashMap<>();
	}

	// === 只读的方法 直接返回

	public V get(Object key) {
		return internalMap.get(key);
	}

	@Override
	public int size() {
		return internalMap.size();
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}

	@Override
	public Set<K> keySet() {
		return internalMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return internalMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return internalMap.entrySet();
	}
}

package com.pangu.framework.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.pangu.framework.resource.other.Getter;
import com.pangu.framework.resource.other.GetterBuilder;
import com.pangu.framework.resource.other.IndexGetter;
import com.pangu.framework.resource.other.InjectDefinition;
import com.pangu.framework.resource.other.ResourceDefinition;
import com.pangu.framework.resource.reader.ReaderHolder;
import com.pangu.framework.resource.reader.ResourceReader;
import com.pangu.framework.utils.json.JsonUtils;
import org.springframework.core.io.ResourceLoader;

/**
 * 存储空间对象
 * @author frank
 */
public class I18NStorage<K, V> extends Storage<K, V> {
	private static final Logger logger = LoggerFactory.getLogger(Storage.class);

	public static final String DEFAULT_LANG = "zh_cn";

	@Autowired
	private ReaderHolder readerHolder;
	/** 资源定义 */
	private ResourceDefinition resourceDefinition;
	/** 资源读取器 */
	private ResourceReader reader;
	/** 标识获取器 */
	private Getter identifier;
	/** 索引获取器集合 */
	private Map<String, IndexGetter> indexGetters;

	/** 全部语言 */
	private Set<String> langs = new HashSet<>();
	/** 主存储空间 */
	private ConcurrentHashMap<String, Map<K, V>> values = new ConcurrentHashMap<>();
	/** 索引存储空间 */
	private ConcurrentHashMap<String, Map<String, Map<Object, List<V>>>> indexs = new ConcurrentHashMap<>();
	/** 唯一值存储空间 */
	private ConcurrentHashMap<String, Map<String, Map<Object, V>>> uniques = new ConcurrentHashMap<>();

	/** 已初始化标识 */
	private boolean initialized;

	/** 读取锁 */
	private final Lock readLock;
	/** 写入锁 */
	private final Lock writeLock;

	public I18NStorage() {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}

	/**
	 * 初始化方法，仅需运行一次
	 * @param definition
	 */
	@Override
	public synchronized void initialize(ResourceDefinition definition) {
		if (initialized) {
			return; // 避免重复初始化
		}

		// 设置初始化标识
		this.initialized = true;
		// 获取资源信息
		this.resourceDefinition = definition;
		this.reader = readerHolder.getReader(definition.getFormat().getType());
		this.identifier = GetterBuilder.createIdGetter(definition.getClz());
		this.indexGetters = GetterBuilder.createIndexGetters(definition.getClz());
		// 注入静态属性
		if (resourceLoader instanceof ApplicationContext) {
			ApplicationContext applicationContext = (ApplicationContext) resourceLoader;
			Set<InjectDefinition> injects = definition.getStaticInjects();
			for (InjectDefinition inject : injects) {
				Field field = inject.getField();
				Object injectValue = inject.getValue(applicationContext);
				try {
					field.set(null, injectValue);
				} catch (Exception e) {
					FormattingTuple message = MessageFormatter.format("无法注入静态资源[{}]的[{}]属性值",
							definition.getClz().getName(), inject.getField().getName());
					logger.error(message.getMessage());
					throw new IllegalStateException(message.getMessage());
				}
			}
		}

		// 加载静态资源
		this.load();
	}

	/**
	 * 重新加载静态资源
	 */
	@Override
	public void reload() {
		clear();
		load();
		validate();
	}

	/**
	 * 重新加载静态资源
	 */
	public void validate() {
		for (String lang : langs) {
			this.validate(lang);
		}
	}

	/**
	 * 获取指定键对应的静态资源实例
	 * @param key 键
	 * @param flag 不存在时是否抛出异常,true:不存在时抛出异常,false:不抛出异常返回null
	 * @return
	 */
	@Override
	public V get(K key, boolean flag) {
		return get(DEFAULT_LANG, key, flag);
	}

	/**
	 * 获取指定键对应的静态资源实例
	 * @param key 键
	 * @param flag 不存在时是否抛出异常,true:不存在时抛出异常,false:不抛出异常返回null
	 * @return
	 */
	public V get(String lang, K key, boolean flag) {
		isReady();
		readLock.lock();
		try {
			Map<K, V> nlcs = getNlcValues(lang);
			V result = nlcs.get(key);
			if (flag && result == null) {
				FormattingTuple message = MessageFormatter.format("标识为[{}]的静态资源[{}]不存在", key, getClz().getName());
				logger.error(message.getMessage());
				throw new IllegalStateException(message.getMessage());
			}
			return result;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 是否包含了指定的主键
	 * @param key
	 * @return
	 */
	@Override
	public boolean containsId(K key) {
		return containsId(DEFAULT_LANG, key);
	}

	/**
	 * 是否包含了指定的主键
	 * @param key
	 * @return
	 */
	public boolean containsId(String lang, K key) {
		isReady();
		readLock.lock();
		try {
			Map<K, V> nlcs = getNlcValues(lang);
			return nlcs.containsKey(key);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 获取全部的静态资源实例
	 * @return 返回的集合是只读的，不能进行元素的添加或移除
	 */
	@Override
	public Collection<V> getAll() {
		return getAll(DEFAULT_LANG);
	}

	/**
	 * 获取全部的静态资源实例
	 * @return 返回的集合是只读的，不能进行元素的添加或移除
	 */
	public Collection<V> getAll(String lang) {
		isReady();
		readLock.lock();
		try {
			Map<K, V> nlcs = getNlcValues(lang);
			if (nlcs.isEmpty()) {
				nlcs = getNlcValues(DEFAULT_LANG);
			}

			return Collections.unmodifiableCollection(nlcs.values());
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 获取指定的唯一索引实例
	 * @param name 唯一索引名
	 * @param value 唯一索引值
	 * @return 不存在会返回 null
	 */
	@Override
	public V getUnique(String name, Object value) {
		return getUnique(DEFAULT_LANG, name, value);
	}

	/**
	 * 获取指定的唯一索引实例
	 * @param name 唯一索引名
	 * @param value 唯一索引值
	 * @return 不存在会返回 null
	 */
	public V getUnique(String lang, String name, Object value) {
		isReady();
		readLock.lock();
		try {
			Map<String, Map<Object, V>> nlcs = getNlcUnique(lang);
			if (nlcs.isEmpty()) {
				nlcs = getNlcUnique(DEFAULT_LANG);
			}

			Map<Object, V> index = nlcs.get(name);
			if (index == null) {
				return null;
			}
			return index.get(value);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 获取指定的索引内容列表
	 * @param name 索引名
	 * @param value 索引值
	 * @return 不存在会返回{@link Collections#EMPTY_LIST}
	 */
	@Override
	public List<V> getIndex(String name, Object value) {
		return getIndex(DEFAULT_LANG, name, value);
	}

	/**
	 * 获取指定的索引内容列表
	 * @param name 索引名
	 * @param value 索引值
	 * @return 不存在会返回{@link Collections#EMPTY_LIST}
	 */
	@SuppressWarnings("unchecked")
	public List<V> getIndex(String lang, String name, Object value) {
		isReady();
		readLock.lock();
		try {
			Map<String, Map<Object, List<V>>> nlcs = getNlcIndex(lang);
			if (nlcs.isEmpty()) {
				nlcs = getNlcIndex(DEFAULT_LANG);
			}

			Map<Object, List<V>> index = nlcs.get(name);
			if (index == null) {
				return Collections.EMPTY_LIST;
			}
			List<V> indexList = index.get(value);
			if (indexList == null) {
				return Collections.EMPTY_LIST;
			}
			ArrayList<V> result = new ArrayList<V>(indexList);
			return result;
		} finally {
			readLock.unlock();
		}
	}

	// -----

	/**
	 * 检查是否已经初始化完成
	 * @return
	 */
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * 获取静态资源路径
	 * @return
	 */
	@Override
	public String getLocation() {
		return getLocation(DEFAULT_LANG);
	}

	/**
	 * 获取静态资源路径
	 * @return
	 */
	public String getLocation(String nls) {
		String location = resourceDefinition.getLocation();
		String path = resourceDefinition.getPath();
		return location + ResourceDefinition.FILE_PATH + nls + ResourceDefinition.FILE_PATH + path;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<V> getClz() {
		return (Class<V>) resourceDefinition.getClz();
	}

	// 内部方法

	private Map<K, V> getNlcValues(String lang) {
		Map<K, V> nlcs = values.get(lang);
		if (nlcs == null) {
			nlcs = new LinkedHashMap<>();
			Map<K, V> old = values.putIfAbsent(lang, nlcs);
			if (old != null) {
				nlcs = old;
			}
		}
		return nlcs;
	}

	private Map<String, Map<Object, List<V>>> getNlcIndex(String lang) {
		Map<String, Map<Object, List<V>>> nlcs = indexs.get(lang);
		if (nlcs == null) {
			nlcs = new HashMap<>();
			Map<String, Map<Object, List<V>>> old = indexs.putIfAbsent(lang, nlcs);
			if (old != null) {
				nlcs = old;
			}
		}
		return nlcs;
	}

	private Map<String, Map<Object, V>> getNlcUnique(String lang) {
		Map<String, Map<Object, V>> nlcs = uniques.get(lang);
		if (nlcs == null) {
			nlcs = new HashMap<>();
			Map<String, Map<Object, V>> old = uniques.putIfAbsent(lang, nlcs);
			if (old != null) {
				nlcs = old;
			}
		}
		return nlcs;
	}

	private void load() {
		isReady();
		// 生成全部语言列表
		buildLangs();

		for (String lang : langs) {
			this.load(lang);
		}
	}

	/** 生成全部语言列表 */
	private void buildLangs() {
		Resource resource = resourceLoader.getResource(resourceDefinition.getLocation());
		boolean exits = false;
		HashSet<String> temps = new HashSet<>();
		temps.add(DEFAULT_LANG);
		try {
			File dir = resource.getFile();
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				for (File sub : files) {
					if (!sub.isDirectory()) {
						continue;
					}
					String name = sub.getName();
					if (name.equals(DEFAULT_LANG)) {
						exits = true;
					} else {
						temps.add(name);
					}
				}
			}
		} catch (IOException e) {
			FormattingTuple message = MessageFormatter.format("资源文件路径[{}]不存在", getLocation());
			logger.error(message.getMessage());
			throw new IllegalStateException(message.getMessage());
		}

		if (exits) {
			writeLock.lock();
			try {
				this.langs = temps;
			} finally {
				writeLock.unlock();
			}
		} else {
			FormattingTuple message = MessageFormatter.format("资源文件路径[{}]不存在", getLocation());
			logger.error(message.getMessage());
			throw new IllegalStateException(message.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void load(String lang) {
		isReady();
		writeLock.lock();
		InputStream input = null;
		try {
			// 获取数据源
			Resource resource = resourceLoader.getResource(getLocation(lang));
			input = resource.getInputStream();
			// 获取存储空间
			Iterator<V> it = reader.read(input, getClz()).iterator();

			while (it.hasNext()) {
				V obj = it.next();
				// 注入 Spring 容器的内容
				if (resourceLoader instanceof ApplicationContext) {
					ApplicationContext applicationContext = (ApplicationContext) resourceLoader;
					Set<InjectDefinition> injects = resourceDefinition.getInjects();
					for (InjectDefinition inject : injects) {
						Field field = inject.getField();
						Object value = inject.getValue(applicationContext);
						try {
							field.set(obj, value);
						} catch (Exception e) {
							logger.error("设置静态对象[{}]属性[{}]时出现异常", resourceDefinition.getClz().getSimpleName(), field.getName(), e);
						}
					}
				}

				if (put(lang, obj) != null) {
					FormattingTuple message = MessageFormatter.format("[{}]资源[{}]的唯一标识重复", getClz(),
							JsonUtils.object2String(obj));
					logger.error(message.getMessage());
					throw new IllegalStateException(message.getMessage());
				}
			}
			Map<String, Map<Object, List<V>>> nlcs = getNlcIndex(lang);
			for (Entry<String, Map<Object, List<V>>> entry : nlcs.entrySet()) {
				String key = entry.getKey();
				IndexGetter getter = indexGetters.get(key);
				if (getter.hasComparator()) {
					for (List<V> values : entry.getValue().values()) {
						Collections.sort(values, getter.getComparator());
					}
				}
			}
			// 通知监听器
			this.setChanged();
			this.notifyObservers();
		} catch (IOException e) {
			FormattingTuple message = MessageFormatter.format("静态资源[{}]所对应的资源文件[{}]不存在", getClz().getName(),
					getLocation(lang));
			logger.error(message.getMessage());
			throw new IllegalStateException(message.getMessage());
		} catch (ClassCastException e) {
			FormattingTuple message = MessageFormatter.format("静态资源[{}]配置的索引内容排序器不正确", getClz().getName(), e);
			logger.error(message.getMessage());
			throw new IllegalStateException(message.getMessage(), e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
				}
			}
			writeLock.unlock();
		}
	}

	/**
	 * 重新加载静态资源
	 */
	private void validate(String lang) {
		isReady();
		writeLock.lock();
		try {
			Map<K, V> nlcs = getNlcValues(lang);
			for (V obj : nlcs.values()) {
				// 静态数据是否合法的检查
				if (resourceDefinition.isNeedValidate()) {
					boolean pass = false;
					try {
						pass = ((Validate) obj).isValid();
					} catch (Exception e) {
						logger.error("进行静态数据[{}]校验时出现异常", resourceDefinition.getClz().getSimpleName(), e);
					} finally {
						if (!pass) {
							Object id = identifier.getValue(obj);
							FormattingTuple message = MessageFormatter.format("静态数据[{}:{}]校验失败", resourceDefinition
									.getClz().getSimpleName(), id);
							logger.error(message.getMessage());
							throw new RuntimeException(message.getMessage());
						}
					}
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * 检查是否初始化就绪
	 * @throws RuntimeException 未初始化时抛出
	 */
	private void isReady() {
		if (!isInitialized()) {
			String message = "未初始化完成";
			logger.error(message);
			throw new RuntimeException(message);
		}
	}

	/**
	 * 清空全部存储空间
	 */
	private void clear() {
		langs.clear();
		values.clear();
		indexs.clear();
		uniques.clear();
	}

	private V put(String lang, V value) {
		// 唯一标识处理
		@SuppressWarnings("unchecked")
		K key = (K) identifier.getValue(value);
		if (key == null) {
			FormattingTuple message = MessageFormatter.format("静态资源[{}]存在标识属性为null的配置项", getClz().getName());
			logger.error(message.getMessage());
			throw new RuntimeException(message.getMessage());
		}
		Map<K, V> nlcs = getNlcValues(lang);
		V result = nlcs.put(key, value);

		// 索引处理
		for (IndexGetter getter : indexGetters.values()) {
			String name = getter.getName();
			Object indexKey = getter.getValue(value);
			// 索引内容存储
			if (getter.isUnique()) {
				Map<Object, V> index = loadUniqueIndex(lang, name);
				if (index.put(indexKey, value) != null) {
					FormattingTuple message = MessageFormatter.arrayFormat("[{}]资源的唯一索引[{}]的值[{}]重复", new Object[] {
						getClz().getName(), name, indexKey });
					logger.debug(message.getMessage());
					throw new RuntimeException(message.getMessage());
				}
			} else {
				Map<Object, List<V>> indexs = loadListIndex(lang, name, indexKey);
				for (Entry<Object, List<V>> e : indexs.entrySet()) {
					List<V> list = e.getValue();
					list.add(value);
				}
			}
		}

		return result;
	}

	private Map<Object, List<V>> loadListIndex(String lang, String name, Object indexKey) {
		Map<Object, List<V>> index = loadListIndex(lang, name);
		Map<Object, List<V>> result = null;
		if (indexKey != null) {
			if (indexKey.getClass().isArray()) {
				Object[] array = (Object[]) indexKey;
				result = new HashMap<>(array.length);
				for (Object key : array) {
					if (index.containsKey(key)) {
						result.put(key, index.get(key));
					} else {
						List<V> list = new LinkedList<>();
						index.put(key, list);
						result.put(key, list);
					}
				}
			} else if (indexKey instanceof Collection) {
				Collection<?> array = (Collection<?>) indexKey;
				result = new HashMap<>(array.size());
				for (Object key : array) {
					if (index.containsKey(key)) {
						result.put(key, index.get(key));
					} else {
						List<V> list = new LinkedList<>();
						index.put(key, list);
						result.put(key, list);
					}
				}
			}
		}
		if (result == null) {
			result = new HashMap<>(1);
			Object key = indexKey;
			if (index.containsKey(key)) {
				result.put(key, index.get(key));
			} else {
				List<V> list = new LinkedList<>();
				index.put(key, list);
				result.put(key, list);
			}
		}
		return result;
	}

	private Map<Object, List<V>> loadListIndex(String lang, String name) {
		Map<String, Map<Object, List<V>>> nlcs = getNlcIndex(lang);
		if (nlcs.containsKey(name)) {
			return nlcs.get(name);
		}

		Map<Object, List<V>> result = new HashMap<Object, List<V>>();
		nlcs.put(name, result);
		return result;
	}

	private Map<Object, V> loadUniqueIndex(String lang, String name) {
		Map<String, Map<Object, V>> nlcs = getNlcUnique(lang);
		if (nlcs.containsKey(name)) {
			return nlcs.get(name);
		}

		Map<Object, V> result = new HashMap<Object, V>();
		nlcs.put(name, result);
		return result;
	}

	// 实现Spring的接口
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}

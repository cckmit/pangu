package com.pangu.framework.resource;

import com.pangu.framework.utils.reflect.ReflectionUtils;
import com.pangu.framework.resource.other.*;
import com.pangu.framework.resource.reader.ReaderHolder;
import com.pangu.framework.resource.reader.ResourceReader;
import com.pangu.framework.utils.json.JsonUtils;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class Storage<K, V> extends OrderObservable implements ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(Storage.class);

    @Autowired
    @Setter
    private ReaderHolder readerHolder;

    /**
     * 已初始化标识
     */
    private boolean initialized;
    /**
     * 资源定义
     */
    private ResourceDefinition resourceDefinition;

    /**
     * 初始化方法，仅需运行一次
     *
     * @param definition
     */
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
        long start = System.currentTimeMillis();
        this.load();
        long end = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            String info = String.format("加载 %s  时间: %.2f 秒 数据: %d", resourceDefinition.getPath(), (end - start) * 0.001, this.innerValues.values.size());
            logger.debug(info);
        }
    }

    /**
     * 资源读取器
     */
    private ResourceReader reader;
    /**
     * 标识获取器
     */
    private Getter identifier;
    /**
     * 索引获取器集合
     */
    private Map<String, IndexGetter> indexGetters;

    /**
     * 主存储空间
     */
    private volatile InnerValues innerValues = new InnerValues(0);

    /**
     * 获取指定键对应的静态资源实例
     *
     * @param key  键
     * @param flag 不存在时是否抛出异常,true:不存在时抛出异常,false:不抛出异常返回null
     * @return
     */
    public V get(K key, boolean flag) {
        isReady();
        V result = innerValues.values.get(key);
        if (flag && result == null) {
            FormattingTuple message = MessageFormatter.format("标识为[{}]的静态资源[{}]不存在", key, getClz().getName());
            logger.error(message.getMessage());
            throw new IllegalStateException(message.getMessage());
        }
        return result;
    }

    /**
     * 是否包含了指定的主键
     *
     * @param key
     * @return
     */
    public boolean containsId(K key) {
        isReady();
        return innerValues.values.containsKey(key);
    }

    /**
     * 获取全部的静态资源实例
     *
     * @return 返回的集合是只读的，不能进行元素的添加或移除
     */
    public Collection<V> getAll() {
        isReady();
        return Collections.unmodifiableCollection(innerValues.values.values());
    }

    /**
     * 获取指定的唯一索引实例
     *
     * @param name  唯一索引名
     * @param value 唯一索引值
     * @return 不存在会返回 null
     */
    public V getUnique(String name, Object value) {
        isReady();
        Map<Object, V> index = innerValues.uniques.get(name);
        if (index == null) {
            return null;
        }
        return index.get(value);
    }

    /**
     * 获取指定的索引内容列表
     *
     * @param name  索引名
     * @param value 索引值
     * @return 不存在会返回{@link Collections#EMPTY_LIST}
     */
    @SuppressWarnings("unchecked")
    public List<V> getIndex(String name, Object value) {
        isReady();
        Map<Object, List<V>> index = innerValues.indexs.get(name);
        if (index == null) {
            return Collections.EMPTY_LIST;
        }
        List<V> indexList = index.get(value);
        if (indexList == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(indexList);
    }

    /**
     * 重新加载静态资源
     */
    public void reload() {
        this.load();
    }

    @SuppressWarnings({"unchecked"})
    private void load() {
        isReady();
        InputStream input = null;
        try {
            // 获取数据源
            Resource resource = resourceLoader.getResource(getLocation());
            input = resource.getInputStream();
            // 获取存储空间
            List<V> list = reader.read(input, getClz());
            Iterator<V> it = list.iterator();

            InnerValues innerValues = new InnerValues(list.size());
            while (it.hasNext()) {
                V obj = it.next();
                // 注入 Spring 容器的内容
                Set<InjectDefinition> injects = resourceDefinition.getInjects();
                if (injects.size() > 0) {
                    if (resourceLoader instanceof ApplicationContext) {
                        ApplicationContext applicationContext = (ApplicationContext) resourceLoader;
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
                }

                if (innerValues.put(obj) != null) {
                    K key = (K) identifier.getValue(obj);
                    FormattingTuple message = MessageFormatter.format("[{}]资源[{}]的唯一标识重复", getClz(),
                            JsonUtils.object2String(key));
                    logger.error(message.getMessage());
                    throw new IllegalStateException(message.getMessage());
                }
            }

            innerValues.sortIndex();

            // 校验每条记录有效性
            validate(innerValues.values);

            this.innerValues = innerValues;

        } catch (IOException e) {
            FormattingTuple message = MessageFormatter.format("静态资源[{}]所对应的资源文件[{}]不存在", getClz().getName(),
                    getLocation());
            logger.error(message.getMessage());
            throw new IllegalStateException(message.getMessage(), e);
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
        }
        // 通知监听器
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * 重新加载静态资源
     */
    public void validate() {
        validate(this.innerValues.values);
    }

    private void validate(Map<K, V> values) {
        isReady();
        for (V obj : values.values()) {
            // 将集合字段全部设置为不可修改
            changeValueFieldsToUnModify(obj);
            // 静态数据是否合法的检查
            if (resourceDefinition.isNeedValidate()) {
                Object id = identifier.getValue(obj);
                FormattingTuple message = MessageFormatter.format("静态数据[{}:{}]校验失败", resourceDefinition
                        .getClz().getSimpleName(), id);
                try {
                    boolean pass = ((Validate) obj).isValid();
                    if (!pass) {
                        logger.error(message.getMessage());
                        throw new RuntimeException(message.getMessage());
                    }
                } catch (Exception e) {
                    logger.error(message.getMessage(), e);
                    throw new RuntimeException(message.getMessage());
                }
            }
        }
    }

    private void changeValueFieldsToUnModify(V obj) {
        Class<?> clz = obj.getClass();
        ReflectionUtils.doWithDeclaredFields(clz, field -> {
            Class<?> type = field.getType();
            field.setAccessible(true);
            if (Map.class == type) {
                Map ori = (Map) field.get(obj);
                if (ori == null) {
                    return;
                }
                Map unmodify = Collections.unmodifiableMap(ori);
                field.set(obj, unmodify);
            } else if (Set.class == type) {
                Set ori = (Set) field.get(obj);
                if (ori == null) {
                    return;
                }
                Set unmodify = Collections.unmodifiableSet(ori);
                field.set(obj, unmodify);
            } else if (List.class == type) {
                List ori = (List) field.get(obj);
                if (ori == null) {
                    return;
                }
                List unmodify = Collections.unmodifiableList(ori);
                field.set(obj, unmodify);
            }
        }, field -> true);
    }

    /**
     * 检查是否已经初始化完成
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取静态资源路径
     *
     * @return
     */
    public String getLocation() {
        return resourceDefinition.getLocation() + "/" + resourceDefinition.getPath();
    }

    // 内部方法

    /**
     * 检查是否初始化就绪
     *
     * @throws RuntimeException 未初始化时抛出
     */
    private void isReady() {
        if (!isInitialized()) {
            String message = "未初始化完成";
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    @SuppressWarnings("unchecked")
    public Class<V> getClz() {
        return (Class<V>) resourceDefinition.getClz();
    }

    // 封装内部数据读取，读取数据不直接修改全部数据，防止reload失败，导致一半数据生效
    private class InnerValues {
        /**
         * 主存储空间
         */
        private final Map<K, V> values;
        /**
         * 索引存储空间
         */
        private final Map<String, Map<Object, List<V>>> indexs;
        /**
         * 唯一值存储空间
         */
        private final Map<String, Map<Object, V>> uniques;

        InnerValues(int size) {
            this.values = new HashMap<>(size);
            int idxCount = indexGetters == null ? 0 : indexGetters.size();
            this.indexs = new HashMap<>(idxCount);
            this.uniques = new HashMap<>(idxCount);
        }

        private V put(V value) {
            // 唯一标识处理
            @SuppressWarnings("unchecked")
            K key = (K) identifier.getValue(value);
            if (key == null) {
                FormattingTuple message = MessageFormatter.format("静态资源[{}]存在标识属性为null的配置项", getClz().getName());
                logger.error(message.getMessage());
                throw new RuntimeException(message.getMessage());
            }
            V result = values.put(key, value);

            // 索引处理
            for (IndexGetter getter : indexGetters.values()) {
                String name = getter.getName();
                Object indexKey = getter.getValue(value);
                // 索引内容存储
                if (getter.isUnique()) {
                    Map<Object, V> index = loadUniqueIndex(name);
                    if (index.put(indexKey, value) != null) {
                        FormattingTuple message = MessageFormatter.arrayFormat("[{}]资源的唯一索引[{}]的值[{}]重复", new Object[]{
                                getClz().getName(), name, indexKey});
                        logger.debug(message.getMessage());
                        throw new RuntimeException(message.getMessage());
                    }
                } else {
                    putListIndex(name, indexKey, value);
                }
            }

            return result;
        }

        // 索引key可能是数组，所以，给每个数组的值都增加一个空的列表
        private void putListIndex(String name, Object indexKey, V value) {
            Map<Object, List<V>> index = loadListIndex(name);
            if (indexKey == null) {
                List<V> vs = index.computeIfAbsent(null, k -> new ArrayList<>());
                vs.add(value);
                return;
            }
            if (indexKey.getClass().isArray()) {
                Object[] array = (Object[]) indexKey;
                for (Object key : array) {
                    List<V> vs = index.computeIfAbsent(key, k -> new ArrayList<>());
                    vs.add(value);
                }
            } else if (indexKey instanceof Collection) {
                Collection<?> array = (Collection<?>) indexKey;
                for (Object key : array) {
                    List<V> vs = index.computeIfAbsent(key, k -> new ArrayList<>());
                    vs.add(value);
                }
            } else {
                List<V> vs = index.computeIfAbsent(indexKey, k -> new ArrayList<>());
                vs.add(value);
            }
        }

        private Map<Object, List<V>> loadListIndex(String name) {
            if (indexs.containsKey(name)) {
                return indexs.get(name);
            }

            Map<Object, List<V>> result = new HashMap<>();
            indexs.put(name, result);
            return result;
        }

        private Map<Object, V> loadUniqueIndex(String name) {
            Map<Object, V> map = uniques.get(name);
            if (map != null) {
                return map;
            }

            Map<Object, V> result = new HashMap<>();
            uniques.put(name, result);
            return result;
        }

        @SuppressWarnings({"unchecked"})
        void sortIndex() {
            // 对排序索引进行排序
            for (Entry<String, Map<Object, List<V>>> entry : indexs.entrySet()) {
                String key = entry.getKey();
                IndexGetter getter = indexGetters.get(key);
                if (getter.hasComparator()) {
                    for (List<V> values : entry.getValue().values()) {
                        values.sort(getter.getComparator());
                    }
                }
            }
        }
    }

    // 实现Spring的接口

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

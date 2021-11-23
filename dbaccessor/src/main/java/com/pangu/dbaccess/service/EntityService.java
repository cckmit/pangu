package com.pangu.dbaccess.service;

import com.pangu.core.common.ServerInfo;
import com.pangu.core.db.facade.DbFacade;
import com.pangu.core.db.model.EntityRes;
import com.pangu.dbaccess.config.EntityConfig;
import com.pangu.dbaccess.config.EntityConfigParser;
import com.pangu.dbaccess.config.FieldDesc;
import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.handler.param.ProtocolCoder;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.lang.NumberUtils;
import com.pangu.framework.utils.model.Result;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityService {

    private final IDbServerAccessor dbServerAccessor;
    private final ClientFactory clientFactory;
    private final ProtocolCoder protocolCoder;

    private final ConcurrentHashMap<Class<?>, EntityConfig> configs = new ConcurrentHashMap<>();

    public EntityService(IDbServerAccessor dbServerAccessor, ClientFactory clientFactory, ProtocolCoder protocolCoder) {
        this.dbServerAccessor = dbServerAccessor;
        this.clientFactory = clientFactory;
        this.protocolCoder = protocolCoder;
    }

    public <PK extends Comparable<PK> & Serializable, T> T load(String userServerId, Class<T> clz, PK pk) {
        EntityConfig entityConfig = getEntityConfig(clz);
        String idColumnName = entityConfig.getIdName();
        return loadOne(userServerId, clz, idColumnName, pk);
    }

    private <COL extends Comparable<COL> & Serializable, T> T loadOne(String userServerId, Class<T> clz, String columnName, COL colValue) {
        EntityConfig entityConfig = getEntityConfig(clz);
        DbFacade proxy = getDbFacade(userServerId);
        Result<EntityRes> result = proxy.load(userServerId, entityConfig.getTableName(), columnName, colValue);
        if (result.getCode() < 0) {
            String msg = String.format("服%s检索表%s通过id列%s值%s错误%d", userServerId,
                    entityConfig.getTableName(),
                    columnName,
                    colValue,
                    result.getCode());
            throw new IllegalStateException(msg);
        }
        EntityRes content = result.getContent();
        if (content == null) {
            return null;
        }
        if (content.isError()) {
            throw new IllegalStateException(content.getMsg());
        }
        Map<String, Object> columns = content.getColumns();
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        T instance;
        try {
            instance = clz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        List<FieldDesc> fieldDesc = entityConfig.getFieldDesc();
        for (FieldDesc desc : fieldDesc) {
            Object value = columns.remove(desc.getColumnName());
            if (value == null) {
                continue;
            }
            Type fileType = desc.getFileType();
            if (fileType instanceof Class<?>) {
                Class<?> ct = (Class<?>) fileType;
                if (Number.class.isAssignableFrom(ct)) {
                    if (value instanceof Number) {
                        Object o = NumberUtils.valueOf(fileType, (Number) value);
                        try {
                            desc.getField().set(instance, o);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    } else if (value instanceof String) {
                        Object o = NumberUtils.valueOf(fileType, (String) value);
                        try {
                            desc.getField().set(instance, o);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    } else {
                        String msg = String.format("数字类型解析异常%s,%s", value.getClass(), value);
                        throw new IllegalStateException(msg);
                    }
                    continue;
                }
                try {
                    desc.getField().set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                Object ret = JsonUtils.string2Object((String) value, fileType);
                try {
                    desc.getField().set(instance, ret);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return instance;
    }

    private DbFacade getDbFacade(String userServerId) {
        ServerInfo serverInfo = getServerInfo(userServerId);
        Client client = clientFactory.getClient(serverInfo.getAddress());
        client.setCoder(protocolCoder);
        return client.getProxy(DbFacade.class);
    }

    private <T> EntityConfig getEntityConfig(Class<T> clz) {
        EntityConfig entityConfig = configs.get(clz);
        if (entityConfig == null) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (clz) {
                entityConfig = configs.get(clz);
                if (entityConfig == null) {
                    entityConfig = EntityConfigParser.parse(clz);
                    configs.put(clz, entityConfig);
                }
            }
        }
        return entityConfig;
    }

    public <PK extends Comparable<PK> & Serializable, T> T loadOrCreate(String userServerId, Class<T> clz, PK pk, EntityBuilder<PK, T> builder) {
        T load = load(userServerId, clz, pk);
        if (load != null) {
            return load;
        }
        T t = builder.newInstance(pk);
        DbFacade dbFacade = getDbFacade(userServerId);
        EntityConfig entityConfig = getEntityConfig(clz);
        List<FieldDesc> fieldDesc = entityConfig.getFieldDesc();
        Map<String, Object> values = new HashMap<>(fieldDesc.size());
        Object id = null;
        for (FieldDesc desc : fieldDesc) {
            Object o;
            try {
                o = desc.getField().get(t);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if ((o instanceof Map) || (o instanceof Collection)) {
                o = JsonUtils.object2String(o);
            }
            values.put(desc.getColumnName(), o);
            if (entityConfig.getIdName().equals(desc.getFieldName())) {
                id = o;
            }
        }
        dbFacade.insert(userServerId, entityConfig.getTableName(), id, values);
        return t;
    }

    public <PK extends Comparable<PK> & Serializable, T> T unique(String userServerId, Class<T> clz, String uniqueName, PK uniqueValue) {
        EntityConfig entityConfig = getEntityConfig(clz);
        Set<String> uniqueNames = entityConfig.getUniqueNames();
        if (uniqueNames == null || !uniqueNames.contains(uniqueName)) {
            throw new IllegalArgumentException("uniqueName参数错误,实体解析验证失败");
        }

        return loadOne(userServerId, clz, uniqueName, uniqueValue);
    }

    public void create(String userServerId, Object entity) {
        Class<?> clz = entity.getClass();
        EntityConfig entityConfig = getEntityConfig(clz);
        List<FieldDesc> fieldDesc = entityConfig.getFieldDesc();
        Map<String, Object> values = new HashMap<>(fieldDesc.size());
        Object id = null;
        for (FieldDesc desc : fieldDesc) {
            Object o;
            try {
                o = desc.getField().get(entity);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if ((o instanceof Map) || (o instanceof Collection)) {
                o = JsonUtils.object2String(o);
            }
            values.put(desc.getColumnName(), o);
            if (entityConfig.getIdName().equals(desc.getFieldName())) {
                id = o;
            }
        }
        DbFacade proxy = getDbFacade(userServerId);
        proxy.insert(userServerId, entityConfig.getTableName(), id, values);
    }

    /**
     * 更新实体
     *
     * @param userServerId
     * @param entity
     */
    public void updateToDB(String userServerId, Object entity) {
        Class<?> clz = entity.getClass();
        EntityConfig entityConfig = getEntityConfig(clz);
        List<FieldDesc> fieldDesc = entityConfig.getFieldDesc();
        Map<String, Object> values = new HashMap<>(fieldDesc.size());
        Object id = null;
        for (FieldDesc desc : fieldDesc) {
            Object o;
            try {
                o = desc.getField().get(entity);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if ((o instanceof Map) || (o instanceof Collection)) {
                o = JsonUtils.object2String(o);
            }
            values.put(desc.getColumnName(), o);
            if (entityConfig.getIdName().equals(desc.getFieldName())) {
                id = o;
            }
        }
        DbFacade proxy = getDbFacade(userServerId);
        proxy.update(userServerId, entityConfig.getTableName(), entityConfig.getIdName(), id, values);
    }

    /**
     * 删除实体
     *
     * @param userServerId
     * @param entity
     */
    public void delete(String userServerId, Object entity) {
        Class<?> clz = entity.getClass();
        EntityConfig entityConfig = getEntityConfig(clz);
        List<FieldDesc> fieldDesc = entityConfig.getFieldDesc();
        Object id = null;
        for (FieldDesc desc : fieldDesc) {
            Object o;
            try {
                o = desc.getField().get(entity);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if ((o instanceof Map) || (o instanceof Collection)) {
                o = JsonUtils.object2String(o);
            }
            if (entityConfig.getIdName().equals(desc.getFieldName())) {
                id = o;
            }
        }
        DbFacade proxy = getDbFacade(userServerId);
        proxy.delete(userServerId, entityConfig.getTableName(), entityConfig.getIdName(), id);
    }

    private ServerInfo getServerInfo(String userServerId) {
        Map<String, ServerInfo> dbs = dbServerAccessor.getDbs();
        if (dbs == null || dbs.isEmpty()) {
            throw new IllegalStateException("没有找到任意一个数据库服");
        }
        Map<String, String> dbManagedServer = dbServerAccessor.getDbManagedServer();
        String dbServerId = dbManagedServer.get(userServerId);
        if (dbServerId == null) {
            throw new IllegalStateException("数据库Id[" + userServerId + "]未找到绑定的DB Server");
        }
        ServerInfo serverInfo = dbs.get(dbServerId);
        if (serverInfo == null) {
            throw new IllegalStateException("数据库检索Db Server[" + dbServerId + "]未启动");
        }
        return serverInfo;
    }

}

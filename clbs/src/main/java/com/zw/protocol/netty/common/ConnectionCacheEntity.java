package com.zw.protocol.netty.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class ConnectionCacheEntity {
    private Map<String, List<ConnectionEntity>> connectionEntityMap;

    public ConnectionCacheEntity() {
        connectionEntityMap = Maps.newConcurrentMap();
    }

    public synchronized Map<String, List<ConnectionEntity>> getConnectionEntityMap() {
        return connectionEntityMap;
    }

    public synchronized List<ConnectionEntity> getConnectionEntityList(String interfaze) {
        return connectionEntityMap.get(interfaze);
    }

    // 客户端连接上线，加入缓存
    public synchronized void online(String interfaze, ConnectionEntity connectionEntity) {
        List<ConnectionEntity> connectionEntityList = retrieveConnectionEntityList(interfaze);
        if (!connectionEntityList.contains(connectionEntity)) {
            connectionEntityList.add(connectionEntity);
        }
    }

    // 客户端连接下线，移出缓存
    public synchronized void offline(String interfaze, ApplicationEntity applicationEntity) {
        if (StringUtils.isNotEmpty(interfaze)) {
            removeConnectionEntity(interfaze, applicationEntity);
        } else {
            removeConnectionEntity(applicationEntity);
        }
    }

    private void removeConnectionEntity(String interfaze, ApplicationEntity applicationEntity) {
        List<ConnectionEntity> connectionEntityList = retrieveConnectionEntityList(interfaze);
        if (CollectionUtils.isEmpty(connectionEntityList)) {
            return;
        }

        for (ConnectionEntity connectionEntity : connectionEntityList) {
            ApplicationEntity entity = connectionEntity.getApplicationEntity();
            if (entity.equals(applicationEntity)) {
                connectionEntityList.remove(connectionEntity);

                break;
            }
        }
    }

    // 复制已存在的连接实体
    public synchronized void duplicateConnectionEntity(String interfaze, ApplicationEntity applicationEntity) {
        ConnectionEntity connectionEntity = getConnectionEntity(applicationEntity);
        online(interfaze, connectionEntity);
    }

    // 判断缓存是否含有上下线信息，如果contains==true，说明上线，反之下线
    // 对于TCP调用，多个Interface共享一个通道
    // 对于HTTP调用，单个Interface占用一个连接
    public synchronized boolean contains(String interfaze, ApplicationEntity applicationEntity) {
        if (StringUtils.isNotEmpty(interfaze)) {
            return getConnectionEntity(interfaze, applicationEntity) != null;
        } else {
            return getConnectionEntity(applicationEntity) != null;
        }
    }

    private synchronized ConnectionEntity getConnectionEntity(String interfaze, ApplicationEntity applicationEntity) {
        List<ConnectionEntity> connectionEntityList = retrieveConnectionEntityList(interfaze);
        if (CollectionUtils.isEmpty(connectionEntityList)) {
            return null;
        }
        for (ConnectionEntity connectionEntity : connectionEntityList) {
            ApplicationEntity entity = connectionEntity.getApplicationEntity();
            if (entity.equals(applicationEntity)) {
                return connectionEntity;
            }
        }

        return null;
    }

    private synchronized ConnectionEntity getConnectionEntity(ApplicationEntity applicationEntity) {
        for (Map.Entry<String, List<ConnectionEntity>> entry : connectionEntityMap.entrySet()) {
            List<ConnectionEntity> connectionEntityList = entry.getValue();
            for (ConnectionEntity connectionEntity : connectionEntityList) {
                ApplicationEntity entity = connectionEntity.getApplicationEntity();
                if (entity.equals(applicationEntity)) {
                    return connectionEntity;
                }
            }
        }

        return null;
    }

    private void removeConnectionEntity(ApplicationEntity applicationEntity) {
        for (Map.Entry<String, List<ConnectionEntity>> entry : connectionEntityMap.entrySet()) {
            List<ConnectionEntity> connectionEntityList = entry.getValue();
            for (ConnectionEntity connectionEntity : connectionEntityList) {
                ApplicationEntity entity = connectionEntity.getApplicationEntity();
                if (entity.equals(applicationEntity)) {
                    connectionEntityList.remove(connectionEntity);

                    break;
                }
            }
        }
    }

    private synchronized List<ConnectionEntity> retrieveConnectionEntityList(String interfaze) {
        List<ConnectionEntity> connectionEntityList = getConnectionEntityList(interfaze);
        if (connectionEntityList == null) {
            connectionEntityList = Lists.newCopyOnWriteArrayList();
            connectionEntityMap.put(interfaze, connectionEntityList);
        }

        return connectionEntityList;
    }

}

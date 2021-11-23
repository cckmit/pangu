package com.pangu.gm.utils;

import com.pangu.core.common.InstanceDetails;
import com.pangu.core.common.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.ArrayList;
import java.util.List;

public class CuratorUtils {

    public static List<ServerInfo> listServers(CuratorFramework framework, String path) {
        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);
        try {
            List<String> strings = framework.getChildren().forPath(path);
            List<ServerInfo> list = new ArrayList<>(strings.size());
            for (String child : strings) {
                byte[] bytes = framework.getData().forPath(path + "/" + child);
                ServiceInstance<InstanceDetails> instance = serializer.deserialize(bytes);
                InstanceDetails payload = instance.getPayload();
                ServerInfo serverInfo = new ServerInfo(instance.getId(), instance.getAddress(), instance.getPort(), payload.getAddressForClient(), payload);
                list.add(serverInfo);
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }
}

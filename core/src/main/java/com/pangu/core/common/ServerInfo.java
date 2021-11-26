package com.pangu.core.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ServerInfo {

    /**
     * 服务器ID
     */
    private String id;

    /**
     * 服务器地址
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * 服务器间请求地址
     */
    private String address;

    /**
     * 给客户端暴露使用的地址
     */
    private String addressForClient;

    /**
     * 附加信息
     */
    private InstanceDetails addition;

    public ServerInfo(String id, String ip, int port, String addressForClient, InstanceDetails addition) {
        this.id = id;
        this.port = port;
        this.address = ip + ":" + port;
        if (addressForClient != null && !addressForClient.contains(":")) {
            this.addressForClient = addressForClient + ":" + port;
        } else {
            this.addressForClient = addressForClient;
        }
        this.addition = addition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

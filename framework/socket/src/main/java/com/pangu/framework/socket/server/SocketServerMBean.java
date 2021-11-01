package com.pangu.framework.socket.server;

/**
 * jmx接口
 */
public interface SocketServerMBean {

    /**
     * 获取绑定接口
     *
     * @return ip:port
     */
    String getBindAddress();

    /**
     * 查看后台线程任务队列长度
     *
     * @return size:size:...
     */
    String getManageQueueSize();

    /**
     * 查看业务线程队列长度
     *
     * @return size:size:...
     */
    String getMessageQueueSize();

    /**
     * 查看同步线程队列长度
     *
     * @return size
     */
    String getSyncQueue();
}

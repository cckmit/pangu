package com.pangu.db.config;

import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.core.common.ZookeeperTask;
import org.apache.curator.framework.recipes.queue.QueueSerializer;

public class TaskQueueSerializer implements QueueSerializer<ZookeeperTask> {
    @Override
    public byte[] serialize(ZookeeperTask item) {
        return JsonUtils.object2Bytes(item);
    }

    @Override
    public ZookeeperTask deserialize(byte[] bytes) {
        return JsonUtils.bytes2Object(bytes, ZookeeperTask.class);
    }
}

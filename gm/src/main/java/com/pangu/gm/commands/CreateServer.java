package com.pangu.gm.commands;

import com.pangu.core.common.Constants;
import com.pangu.core.common.ServerInfo;
import com.pangu.core.common.ZookeeperTask;
import com.pangu.gm.config.GMConfig;
import com.pangu.gm.utils.CuratorUtils;
import com.pangu.gm.utils.TaskQueueSerializer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.SimpleParser;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class CreateServer implements CommandMarker {

    private static final Logger log = HandlerUtils.getLogger(SimpleParser.class);

    private final CuratorFramework curatorFramework;

    private final GMConfig gmConfig;

    public CreateServer(CuratorFramework curatorFramework, GMConfig gmConfig) {
        this.curatorFramework = curatorFramework;
        this.gmConfig = gmConfig;
    }

    @CliCommand(value = "dbs", help = "获取DB服列表")
    public String listDB() {
        List<ServerInfo> serverInfos = CuratorUtils.listServers(curatorFramework, gmConfig.getZookeeper().getRootPath() + "/" + Constants.DB_SERVICE_NAME);
        StringBuilder sb = new StringBuilder();
        for (ServerInfo s : serverInfos) {
            sb.append(s.toString());
            sb.append(OsUtils.LINE_SEPARATOR);
        }
        sb.append("size:").append(serverInfos.size());
        sb.append(OsUtils.LINE_SEPARATOR);
        return sb.toString();
    }

    @CliCommand(value = "createServer", help = "添加一个服,会自动创建数据库,并自动绑定到一台数据服")
    public void createServer(@CliOption(key = "id", help = "服务器id,oid_sid,格式如:2_1") String sid) {
        String queuePath = gmConfig.getZookeeper().getRootPath() + Constants.DB_MASTER_TASK_QUEUE;
        DistributedQueue<ZookeeperTask> queue = QueueBuilder.builder(curatorFramework, null, new TaskQueueSerializer(), queuePath).buildQueue();
        try {
            queue.start();
            queue.put(new ZookeeperTask(Constants.TASK_CREATE_GAME_DATABASE, sid));
            queue.close();
        } catch (Exception e) {
            log.warning("创建任务失败,请检查" + e);
        }
    }
}

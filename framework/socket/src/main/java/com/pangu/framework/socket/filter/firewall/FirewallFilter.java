package com.pangu.framework.socket.filter.firewall;

import com.pangu.framework.socket.filter.SocketFilter;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.utils.IpUtils;
import com.pangu.framework.utils.concurrent.DelayedElement;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 防火墙过滤器
 */
@Sharable
@Slf4j
public class FirewallFilter extends ChannelInboundHandlerAdapter implements FirewallManager, SocketFilter {

    // 分隔符
    public static final String SPLIT = ",";
    // 白名单标识属性
    private static final AttributeKey<Boolean> ATT_ALLOW = AttributeKey.newInstance("firewall:allow");
    // 访问记录属性
    private static final AttributeKey<FirewallRecord> ATT_RECORD = AttributeKey.newInstance("firewall:record");

    // 白名单IP集合
    private final ConcurrentHashMap<String, Pattern> allows = new ConcurrentHashMap<>();
    // 黑名单IP集合
    private final ConcurrentHashMap<String, Pattern> blocks = new ConcurrentHashMap<>();
    // 黑名单移除队列
    private final DelayQueue<DelayedElement<String>> blockRemoveQueue = new DelayQueue<>();
    // 当前客户端连接数
    private final AtomicInteger currentClients = new AtomicInteger();
    // 黑名单阻止时间(默认:10分钟)
    private int blockTimes = 10 * 60;
    // 最大客户端连接数
    private int maxClients = 5000;
    // 阻止全部连接状态(不包括白名单)
    private boolean blockAll = false;

    // ----

    /**
     * 获取连接属性值
     *
     * @param channel
     * @param key
     * @return
     */
    private <T> T getAttribute(Channel channel, AttributeKey<T> key) {
        return channel.attr(key).get();
    }

    /**
     * 获取连接属性值
     *
     * @param ctx
     * @param key
     * @return
     */
    private <T> T getAttribute(ChannelHandlerContext ctx, AttributeKey<T> key) {
        return getAttribute(ctx.channel(), key);
    }

    /**
     * 设置连接属性值
     *
     * @param channel
     * @param key
     * @param value
     */
    private <T> void setAttribute(Channel channel, AttributeKey<T> key, T value) {
        channel.attr(key).set(value);
    }

    /**
     * 设置连接属性值
     *
     * @param ctx
     * @param key
     * @param value
     */
    private <T> void setAttribute(ChannelHandlerContext ctx, AttributeKey<T> key, T value) {
        setAttribute(ctx.channel(), key, value);
    }

    // 对外方法

    @Override
    public Collection<String> getBlockList() {
        return blocks.keySet();
    }

    @Override
    public Collection<String> getAllowList() {
        return allows.keySet();
    }

    @Override
    public void block(String ip) {
        if (blocks.containsKey(ip)) {
            return;
        }
        Pattern pattern = ipToPattern(ip);
        blocks.put(ip, pattern);
        // 加延迟队列
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, blockTimes);
        DelayedElement<String> e = DelayedElement.valueOf(ip, calendar.getTime());
        blockRemoveQueue.add(e);
    }

    @Override
    public void unblock(String ip) {
        blocks.remove(ip);
    }

    @Override
    public void blockAll() {
        blockAll = true;
    }

    @Override
    public void unblockAll() {
        blockAll = false;
    }

    @Override
    public void allow(String ip) {
        if (allows.containsKey(ip)) {
            return;
        }
        Pattern pattern = ipToPattern(ip);
        allows.put(ip, pattern);
    }

    /**
     * 将连接设置为白名单
     *
     * @param session
     */
    public void allow(Session session) {
        Channel channel = session.getChannel();
        if (channel != null) {
            setAttribute(channel, ATT_ALLOW, true);
        }

        String ip = IpUtils.getIp(channel);
        if (allows.containsKey(ip)) {
            return;
        }
        Pattern pattern = ipToPattern(ip);
        allows.put(ip, pattern);
    }

    @Override
    public void disallow(String ip) {
        allows.remove(ip);
    }

    @Override
    public int getCurrentConnections() {
        return currentClients.get();
    }

    // 会话管理方法

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // 增加当前的在线客户端数量
        int clients = currentClients.getAndIncrement();

        // 检查是否白名单
        Channel channel = ctx.channel();
        String ip = IpUtils.getIp(channel);
        for (Pattern pattern : allows.values()) {
            Matcher matcher = pattern.matcher(ip);
            if (matcher.matches()) {
                if (log.isDebugEnabled()) {
                    log.debug("白名单IP[{}]连接服务器", ip);
                }
                setAttribute(ctx, ATT_ALLOW, true);
                super.channelRegistered(ctx);
                return;    // 白名单不进行后续判断,直接跳过
            }
        }

        // 是否禁止全部连接
        if (blockAll) {
            if (log.isDebugEnabled()) {
                log.debug("由于阻止全部连接状态，阻止用户[{}]登录服务器", ip);
            }
            ctx.close();
            return;
        }

        // 最大连接数判断
        if (clients >= maxClients) {
            if (log.isWarnEnabled()) {
                log.warn("到达最大连接数[{}/{}]，非白名单连接将会被拒绝", clients, maxClients);
            }
            // 到达最大连接数，拒绝连接
            ctx.close();
            return;
        }

        // 过期黑名单清理
        for (; ; ) {
            DelayedElement<String> e = blockRemoveQueue.poll();
            if (e == null) {
                break;
            }
            blocks.remove(e.getContent());
        }

        // 检查是否黑名单
        for (Pattern pattern : blocks.values()) {
            Matcher matcher = pattern.matcher(ip);
            if (matcher.matches()) {
                // 是黑名单内的IP，拒绝会话打开
                if (log.isDebugEnabled()) {
                    log.debug("黑名单用户[{}]登录服务器被拒绝", ip);
                }
                ctx.close();
                return;
            }
        }

        super.channelRegistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 减少当前的在线客户端数量
        currentClients.decrementAndGet();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isAllow(ctx)) {
            super.channelRead(ctx, msg);
            return;
        }

        if (check(ctx, msg)) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byteBuf.skipBytes(byteBuf.readableBytes());
            byteBuf.release();

            String ip = IpUtils.getIp(ctx.channel());
            block(ip);
            ctx.close();
            return;
        }
        super.channelRead(ctx, msg);
    }

    /**
     * 检查是否需要阻止该会话
     *
     * @param ctx     会话
     * @param message 信息体
     * @return true:需要;false:不需要
     */
    private boolean check(ChannelHandlerContext ctx, Object message) {
        if (!(message instanceof ByteBuf)) {
            if (log.isDebugEnabled()) {
                log.debug("接收数据类型[{}]不是 IoBuffer", message.getClass().getName());
            }
            return false;
        }

        // 获取记录
        FirewallRecord record = getAttribute(ctx, ATT_RECORD);
        if (record == null) {
            // 记录不存在，创建记录对象
            record = new FirewallRecord();
            setAttribute(ctx, ATT_RECORD, record);
        }

        // 检查本次访问
        int bytes = ((ByteBuf) message).readableBytes();
        if (!record.check(bytes)) {
            return false;
        }

        // 检查是否超出许可
        if (record.isBlock()) {
            String ip = IpUtils.getIp(ctx.channel());
            ByteBuf byteBuf = (ByteBuf) message;
            byte[] data = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(data);
            log.warn("防火墙立即阻止会话[{}]，违规时请求数据包信息[{}]", ip, data);
            return true;
        }

        if (log.isInfoEnabled()) {
            String ip = IpUtils.getIp(ctx.channel());
            log.info("会话[{}]发生违规，违规状态[总违规次数:{} 长度:{} 次数:{}]",
                    ip, record.getViolateTime(), record.getBytesInSecond(), record.getTimesInSecond());
        }
        return false;
    }

    /**
     * 检查是否是白名单
     *
     * @param ctx
     * @return
     */
    private boolean isAllow(ChannelHandlerContext ctx) {
        Boolean result = getAttribute(ctx, ATT_ALLOW);
        return result != null && result;
    }

    /**
     * 将IP地址转换为正则表示形式
     *
     * @param ip
     * @return
     */
    private Pattern ipToPattern(String ip) {
        String reg = ip.replace(".", "[.]").replace("*", "[0-9]*");
        Pattern pattern = Pattern.compile(reg);
        return pattern;
    }

    // Getter and Setter ...

    /**
     * 设置阻止全部连接状态
     *
     * @param state 状态
     */
    public void setBlockAllState(boolean state) {
        blockAll = state;
    }

    /**
     * 设置白名单IP集合
     *
     * @param allows 白名单集合
     */
    public void setAllows(String allows) {
        if (StringUtils.isBlank(allows)) {
            return;
        }
        String[] ips = allows.split(SPLIT);
        for (String ip : ips) {
            String trim = ip.trim();
            this.allows.put(trim, ipToPattern(trim));
        }
    }

    /**
     * 设置永久黑名单
     *
     * @param blocks 黑名单集合
     */
    public void setBlocks(String blocks) {
        if (StringUtils.isBlank(blocks)) {
            return;
        }
        String[] ips = blocks.split(SPLIT);
        for (String ip : ips) {
            String trim = ip.trim();
            this.blocks.put(trim, ipToPattern(trim));
        }
    }

    /**
     * 设置永久黑名单
     *
     * @param ip 黑名单ip
     */
    public void setBlock(String ip) {
        this.blocks.put(ip, ipToPattern(ip));
    }

    /**
     * 设置阻止延时(单位秒)
     *
     * @param blockTimes
     */
    public void setBlockTimes(int blockTimes) {
        this.blockTimes = blockTimes;
    }

    /**
     * 设置最大连接数
     *
     * @param maxClients
     */
    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    /**
     * 设置最大违规次数
     *
     * @param times
     */
    public void setMaxViolateTimes(int times) {
        FirewallRecord.setMaxViolateTimes(times);
    }

    /**
     * 设置每秒收到的字节数限制
     *
     * @param size
     */
    public void setBytesInSecondLimit(int size) {
        FirewallRecord.setBytesInSecondLimit(size);
    }

    /**
     * 设置每秒收到的数据包次数限制
     *
     * @param size
     */
    public void setTimesInSecondLimit(int size) {
        FirewallRecord.setTimesInSecondLimit(size);
    }

    @Override
    public boolean isBlockAll() {
        return this.blockAll;
    }

    @Override
    public int getIndex() {
        return FIRE_WALL;
    }

    @Override
    public String getName() {
        return FIRE_WALL_NAME;
    }
}

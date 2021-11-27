package com.pangu.framework.socket.filter;

import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.utils.IpUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理后台过滤器，用于为特定IP的访问设置管理后台标记
 */
@Sharable
public class ManagementFilter extends ChannelInboundHandlerAdapter implements SocketFilter {

    private LinkedHashMap<Pattern, String> patterns = new LinkedHashMap<>();

    /**
     * 设置管理后台许可IP与对应的管理后台名称
     *
     * @param config key:许可IP的正则 value:名称
     */
    public void setAllowIps(LinkedHashMap<String, String> config) {
        LinkedHashMap<Pattern, String> patterns = new LinkedHashMap<>();
        for (Entry<String, String> entry : config.entrySet()) {
            String ip = entry.getKey();
            String reg = ip.replace(".", "[.]").replace("*", "[0-9]*");
            Pattern pattern = Pattern.compile(reg);
            patterns.put(pattern, entry.getValue());
        }
        this.patterns = patterns;
    }

    /**
     * 设置管理后台许可IP与对应的管理后台名称
     *
     * @param config 内容条目间用","分隔，IP和管理后台名称之间用"="分隔。范例格式:[IP]=[NAME],...
     */
    public void setAllowIpConfig(String config) {
        String[] ips = config.split(",");
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>(ips.length);
        for (String ip : ips) {
            String[] s = ip.split("=", 2);
            result.put(s[0], s[1]);
        }
        setAllowIps(result);
    }

    /**
     * 添加许可IP
     *
     * @param ip   许可的IP
     * @param name 许可名
     */
    public void addAllowIp(String ip, String name) {
        LinkedHashMap<Pattern, String> patterns = new LinkedHashMap<>(this.patterns);

        String reg = ip.replace(".", "[.]").replace("*", "[0-9]*");
        Pattern pattern = Pattern.compile(reg);
        patterns.put(pattern, name);

        this.patterns = patterns;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String ip = IpUtils.getIp(channel);
        for (Entry<Pattern, String> entry : patterns.entrySet()) {
            Matcher matcher = entry.getKey().matcher(ip);
            if (matcher.matches()) {
                // 设置管理后台标记
                Attribute<Boolean> attr = channel.attr(Session.MANAGER);
                attr.set(true);
                break;
            }
        }
        super.channelActive(ctx);
    }

    @Override
    public int getIndex() {
        return MANAGE;
    }

    @Override
    public String getName() {
        return MANAGE_NAME;
    }
}

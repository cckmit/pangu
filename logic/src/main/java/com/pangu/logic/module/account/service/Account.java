package com.pangu.logic.module.account.service;

import com.pangu.dbaccess.anno.Unique;
import com.pangu.framework.utils.ManagedException;
import com.pangu.framework.utils.time.DateUtils;
import com.pangu.logic.module.account.facade.AccountResult;
import com.pangu.logic.module.account.model.AccountState;
import com.pangu.logic.utils.DateHelper;
import com.pangu.core.anno.Enhance;
import lombok.Getter;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Getter
public class Account {

    // 账号名与服务器信息之间的分隔符
    public static final char DOT_SPLIT = '.';

    // 运营商标识与服务器标识之间的分隔符
    public static final char UNDERLINE_SPLIT = '_';

    // 账户编号
    @Id
    private Long id;
    // 账号
    @Column(nullable = false)
    @Unique
    private String name;

    // 创建时间
    @Column(nullable = false)
    private Date createdOn;

    // 状态
    @Column(nullable = false)
    private int state;

    // 是否成年
    private boolean adult;

    // 最后登录时间
    private Date loginOn = new Date();

    // 最后登出时间
    private Date logoutOn = new Date();

    // 当天累计时间
    private long timeByDay;

    // 总在线时间
    private long timeByTotal;

    // 累计登陆天数(从0开始)
    private int loginByTotal;

    // 总在线天数(从0开始)
    private int dayByTotal;

    // 连续登录天数(从0开始)
    private int dayByContinuous;

    // 是否在线状态
    private boolean online;

    // 帐号来源渠道标识
    private String channel;

    // 设备类型
    private String device;

    // 设备标识符
    private String IMEI;

    // 最后登陆IP
    private String ip;

    // 创建IP
    private String createIp;

    // 增强方法

    /**
     * 登陆处理
     *
     * @param now
     * @param isAdult
     * @param ip
     */
    @Enhance
    synchronized void login(Date now, boolean isAdult, String ip) {
        Date lastTime = DateHelper.max(loginOn, logoutOn);
        // 当天首次登陆
        if (!DateUtils.isToday(lastTime)) {
            dayByTotal++;
            loginByTotal++;
            timeByDay = 0;
            if (DateUtils.calcIntervalDays(lastTime, new Date()) > 1) {
                dayByContinuous = 1;
            }
        } else {
            // 上次没有登出时间才计算(--这个在线时长理论上是送给玩家的)
            if (logoutOn.getTime() < loginOn.getTime()) {
                // 修正一次登出时间
                logoutOn = now;
            }
        }
        this.ip = ip;
        this.adult = isAdult;
        this.online = true;
        this.loginOn = now;
    }

    /**
     * 登出处理
     *
     * @param now
     */
    @Enhance
    synchronized void logout(Date now) {
        int day = DateUtils.calcIntervalDays(loginOn, now);
        if (day == 0) {
            // 登入与登出在同一天
            long times = now.getTime() - loginOn.getTime();
            timeByDay += times;
            timeByTotal += times;
        } else {
            // 登入与登出不在同一天
            dayByTotal += day;
            dayByContinuous += day;
            loginByTotal += day;
            timeByTotal += now.getTime() - loginOn.getTime();
            timeByDay += now.getTime() - DateUtils.getFirstTime(now).getTime();
        }
        online = false;
        logoutOn = now;
    }

    /**
     * 添加帐号状态
     *
     * @param state 状态
     */
    @Enhance
    void addState(AccountState state) {
        this.state = this.state | state.getValue();
    }

    /**
     * 移除帐号状态
     *
     * @param state 状态
     */
    @Enhance
    void removeState(AccountState state) {
        this.state = this.state ^ state.getValue();
    }

    @Enhance
    public void updateChannel(String channel) {
        this.channel = channel;
    }

    // 逻辑方法

    /**
     * 检查账号是否存在某种状态
     *
     * @param state 状态
     * @return true标识存在，false标识存在
     */
    public boolean hasState(AccountState state) {
        return (this.state & state.getValue()) == state.getValue();
    }

    /**
     * 获取累计在线天数
     *
     * @return
     */
    public int getTotalDays() {
        return dayByTotal + DateUtils.calcIntervalDays(loginOn, new Date());
    }

    /**
     * 获取连续在线天数
     *
     * @return
     */
    public int getContinuousDays() {
        return dayByContinuous + DateUtils.calcIntervalDays(loginOn, new Date());
    }

    /**
     * 获取累计在线分钟数
     *
     * @return
     */
    public int getTotalMinutes() {
        return (int) (getTotalTimes() / DateUtils.MILLIS_PER_MINUTE);
    }

    /**
     * 获取当天的累计在线分钟数
     *
     * @return
     */
    public int getDayMinutes() {
        return (int) (getDayTimes() / DateUtils.MILLIS_PER_MINUTE);
    }

    /**
     * 获取当天的累计在线秒数
     *
     * @return
     */
    public int getDaySeconds() {
        return (int) (getDayTimes() / DateUtils.MILLIS_PER_SECOND);
    }

    /**
     * 获取累计在线毫秒数
     */
    private long getTotalTimes() {
        Date now = new Date();
        return timeByTotal + now.getTime() - loginOn.getTime();
    }

    /**
     * 获取当天的累计在线毫秒数
     */
    private long getDayTimes() {
        Date now = new Date();
        if (DateUtils.calcIntervalDays(loginOn, now) == 0) {
            return timeByDay + now.getTime() - loginOn.getTime();
        } else {
            boolean today = DateUtils.isToday(createdOn);
            Date first = today ? createdOn : DateUtils.getFirstTime(now);
            return now.getTime() - first.getTime();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Account))
            return false;
        Account other = (Account) obj;
        if (getId() == null) {
            return other.getId() == null;
        } else return getId().equals(other.getId());
    }

    // Static Method's ...

    /**
     * 获取账号名对应的服标识
     *
     * @param name 账号名(包含服标识)
     * @return [运营商标识, 服务器标识]
     */
    public static short[] toOpreatorServer(String name) {
        try {
            String info = name.substring(name.indexOf(DOT_SPLIT) + 1);
            String[] split = info.split("\\" + UNDERLINE_SPLIT);
            return new short[]{Short.parseShort(split[0]), Short.parseShort(split[1])};
        } catch (Exception e) {
            throw new ManagedException(AccountResult.INVAILD_ACCOUNT_NAME, "名为[" + name + "]的账号名非法");
        }
    }

    /**
     * 获取账号名原名
     *
     * @param name 账号名(包含服务器信息)
     * @return
     */
    public static String toOrigin(String name) {
        try {
            return name.substring(0, name.indexOf(DOT_SPLIT));
        } catch (Exception e) {
            throw new ManagedException(AccountResult.INVAILD_ACCOUNT_NAME, "名为[" + name + "]的账号名非法");
        }
    }

    /**
     * 获取账号名服务器信息
     *
     * @param name 账号名(包含服务器信息)
     * @return
     */
    public static String toInfo(String name) {
        try {
            return name.substring(name.indexOf(DOT_SPLIT) + 1);
        } catch (Exception e) {
            throw new ManagedException(AccountResult.INVAILD_ACCOUNT_NAME, "名为[" + name + "]的账号名非法");
        }
    }

    /**
     * 检查帐号是否在指定日期前已经流失
     *
     * @param account 被检查帐号
     * @param days    在多少天前
     * @return
     */
    public static boolean isTurnover(Account account, int days) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days);
        final Date turnover = calendar.getTime();
        if (account.isOnline()) {
            return false;
        }
        return account.getLogoutOn().before(turnover);
    }

    /**
     * 生成存储帐号名
     */
    public static String buildName(int opeartor, int server, String name) {
        return name + DOT_SPLIT + opeartor + UNDERLINE_SPLIT + server;
    }

    /**
     * 生成存储帐号名
     */
    public static String buildName(String server, String name) {
        return name + DOT_SPLIT + server;
    }

    /**
     * 构造方法
     */
    public static Account valueOf(long id, String name, String channel) {
        Account result = new Account();
        result.id = id;
        result.name = name;
        result.channel = channel;
        Date now = new Date();
        result.createdOn = now;
        result.loginOn = now;
        result.logoutOn = now;
        return result;
    }
}

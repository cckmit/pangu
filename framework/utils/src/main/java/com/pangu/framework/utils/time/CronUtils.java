package com.pangu.framework.utils.time;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CRON表达式工具类
 * @author author
 */
public class CronUtils {

	private static final Logger logger = LoggerFactory.getLogger(CronUtils.class);

	private static final String ALL_SPEC = "*";
	private static final String NO_SPEC = "?";
	private static final String DELIMITERS = " ";
	private static final int DAY_OF_MONTH = 3;
	private static final int DAY_OF_WEEK = 5;

	private static final ConcurrentHashMap<String, CronExpression> caches = new ConcurrentHashMap<>();

	private static CronExpression getCronExpression(String cron) {
		return caches.computeIfAbsent(cron, CronUtils::buildCronExpression);
	}

	/**
	 * 构建CronExpression
	 */
	private static CronExpression buildCronExpression(String cron) {
		try {
			return new CronExpression(cron);
		} catch (ParseException e) {
			String cron0 = checkAndFixCron(cron);
			try {
				CronExpression cronExpression = new CronExpression(cron0);
				logger.error("将cron[{}]转换为[{}]", cron, cron0);
				return cronExpression;
			} catch (ParseException e1) {
				throw new IllegalArgumentException(
						"cron：" + cron + "解析错误。" + e.getMessage());
			}
		}
	}

	/**
	 * 检查并修复cron表达式：DAY_OF_MONTH，DAY_OF_WEEK 只指定一个，另外一个设置为?
	 */
	private static String checkAndFixCron(String expression) {
		String[] fields = StringUtils.split(expression, DELIMITERS);
		int noSpecCount = 0;
		noSpecCount += Objects.equals(fields[DAY_OF_MONTH], NO_SPEC) ? 1 : 0;
		noSpecCount += Objects.equals(fields[DAY_OF_WEEK], NO_SPEC) ? 1 : 0;
		if (noSpecCount == 1) {
			return expression;
		} else {
			int index;
			String changeValue;
			if (noSpecCount == 0) {
				//两个都指定，将其中一个*改为?
				if (Objects.equals(fields[DAY_OF_WEEK], ALL_SPEC)) {
					index = DAY_OF_WEEK;
				} else if (Objects.equals(fields[DAY_OF_MONTH], ALL_SPEC)) {
					index = DAY_OF_MONTH;
				} else {
					throw new IllegalArgumentException("cron：" + expression + "解析错误。"
							+ "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented");

				}
				changeValue = NO_SPEC;
			} else {
				//两个都为?，将 DAY_OF_MONTH 改为*
				index = DAY_OF_MONTH;
				changeValue = ALL_SPEC;
			}
			StringBuilder sb = new StringBuilder(expression.length());
			for (int i = 0; i < index; i++) {
				sb.append(fields[i]).append(DELIMITERS);
			}
			sb.append(changeValue);
			for (int i = index + 1; i < fields.length; i++) {
				sb.append(DELIMITERS).append(fields[i]);
			}
			return sb.toString();
		}
	}

	/**
	 * 获取下一次执行时间
	 */
	public static Date getTimeAfter(String cron, Date time) {
		CronExpression exp = getCronExpression(cron);
		return exp.getTimeAfter(time);
	}

	/**
	 * 获取前一次执行时间
	 */
	public static Date getTimeBefore(String cron, Date time) {
		CronExpression exp = getCronExpression(cron);
		return exp.getPrevFireTime(time);
	}

	/**
	 * 获取基准时间后最近的执行时间
	 */
	public static Date getTimeAfter(String[] crons, Date time) {
		long mintime = Long.MAX_VALUE;
		for (String cron : crons) {
			Date afterTime = getTimeAfter(cron, time);
			mintime = Math.min(mintime, afterTime.getTime());
		}
		return new Date(mintime);
	}

	/**
	 * 获取基准时间前最近的执行时间
	 */
	public static Date getTimeBefore(String[] crons, Date time) {
		long mintime = Long.MIN_VALUE;
		for (String cron : crons) {
			Date beforeTime = getTimeBefore(cron, time);
			mintime = Math.max(mintime, beforeTime.getTime());
		}
		return new Date(mintime);
	}
}

package  com.pangu.logic.utils;


import com.pangu.framework.utils.time.DateUtils;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * 记录一个时间段内发生的事件大小
 */
public class TimeEventCounter extends LinkedList<Long> {
    //队尾元素清理时间
    private long clearTime = DateUtils.MILLIS_PER_HOUR;

    public TimeEventCounter() {
    }

    public TimeEventCounter(long clearTime) {
        super();
        this.clearTime = clearTime;
    }

    public long getClearTime() {
        return TimeUnit.MINUTES.convert(clearTime, TimeUnit.MILLISECONDS);
    }

    public synchronized void increase() {
        addFirst(clearTime + System.currentTimeMillis());
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized int size() {
        clearInValidElement();
        return super.size();
    }

    private void clearInValidElement() {
        long current = System.currentTimeMillis();
        while (true) {
            try {
                Long last = getLast();
                if (current > last) {
                    removeLast();
                    continue;
                }
                break;
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }
}

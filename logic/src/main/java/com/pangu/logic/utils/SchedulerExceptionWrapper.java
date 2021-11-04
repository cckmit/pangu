package  com.pangu.logic.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * scheduledService的时候发现一个问题，就是如果Runnable内部运行时抛出异常的话，你的代码是没有任何感知，
 * 因为scheduledService内部已经帮你try- catch
 * author weihongwei
 * date 2017/11/21
 */
@Slf4j
@AllArgsConstructor
public class SchedulerExceptionWrapper implements Runnable {

    Runnable runnable;

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("调度任务异常", e);
            throw e;
        }
    }

    public static SchedulerExceptionWrapper wrapper(Runnable runnable) {
        return new SchedulerExceptionWrapper(runnable);
    }
}

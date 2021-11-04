package  com.pangu.logic.utils;

import com.pangu.framework.utils.rhino.Rhino;
import lombok.extern.slf4j.Slf4j;

/**
 * 公式处理公共类 改为rhino实现
 */
@Slf4j
public class ExpressionHelper {

    /**
     * 执行公式表达式
     *
     * @param expression 公式表达式
     * @param resultType 执行结果类型
     * @param ctx        公式执行上下文
     * @return 公式表达式执行结果
     */
    public static <T> T invoke(String expression, Class<T> resultType, Object ctx) {
        try {
            T result = Rhino.eval(expression, ctx, resultType);
            log.trace("公式内容[{}] 结果[{}]", expression, result);
            return result;
        } catch (RuntimeException e) {
            log.error("公式内容[{}] 执行错误", expression, e);
            throw e;
        }
    }
}

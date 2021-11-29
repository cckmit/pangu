package com.pangu.logic.module.common.resource;

import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 公式对象
 */
@Resource("common")
@Slf4j
public class Formula {

    /**
     * 唯一标识
     */
    @Id
    private String id;
    /**
     * 公式内容
     */
    private String content;
    /**
     * 结果类型
     */
    private Class<?> returnClz;

    public Formula() {
    }

    public Formula(String content, Class<?> returnClz) {
        this.content = content;
        this.returnClz = returnClz;
    }

    /**
     * 根据上下文计算表达式值
     *
     * @param ctx 上下文对象
     * @return
     */
    public Object calculate(Object ctx) {
        Object result = ExpressionHelper.invoke(content, returnClz, ctx);
        log.trace("公式[{}] 内容[{}] 结果[{}]", id, content, result);
        return result;

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map toMap(Object... keyValues) {
        Map result = new HashMap();
        for (int i = 0; i < keyValues.length; i = i + 2) {
            result.put(keyValues[i], keyValues[i + 1]);
        }
        return result;
    }

    // Getter ...

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Class<?> getReturnClz() {
        return returnClz;
    }
}

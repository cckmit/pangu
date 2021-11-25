package com.pangu.framework.socket.handler.param;

import com.pangu.framework.socket.handler.param.type.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Parameters {

    // 参数列表
    private Parameter[] parameters;

    // session必须要授权
    private boolean identity;

    // 需要管理后台IP
    @Setter
    private boolean manager;

    // session中必须存在制定值
    private String[] sessionKeys;

    // 是否存在inBody的值
    private boolean inBody;

    // 只有一个body对象
    private boolean body;

    // 使用future作为参数
    private boolean future;

    // 存在附加信息参数
    private boolean attachment;

    public Parameters(Parameter[] parameters) {
        this.parameters = parameters;
        List<String> sessionKeys = new ArrayList<>(parameters.length);
        boolean body = false;
        boolean inBody = false;
        for (Parameter parameter : parameters) {
            if (parameter instanceof InSessionParameter) {
                if (((InSessionParameter) parameter).isRequired()) {
                    sessionKeys.add(((InSessionParameter) parameter).getKey());
                }
                continue;
            }
            if (parameter instanceof IdentityParameter) {
                if (((IdentityParameter) parameter).isRequired()) {
                    identity = true;
                    continue;
                }
            }
            if (parameter instanceof InBodyParameter) {
                inBody = true;
                continue;
            }
            if (parameter instanceof BodyParameter) {
                body = true;
                continue;
            }
            if (parameter instanceof FutureParameter) {
                future = true;
                continue;
            }
            if (parameter instanceof AttachmentParameter) {
                attachment = true;
            }
        }
        this.body = body;
        this.inBody = inBody;
        this.sessionKeys = sessionKeys.toArray(new String[0]);
    }
}

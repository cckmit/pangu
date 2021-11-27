package com.pangu.framework.socket.handler.command;

import com.pangu.framework.socket.anno.*;
import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.param.Attachment;
import com.pangu.framework.socket.handler.param.Parameters;
import com.pangu.framework.socket.handler.param.type.*;
import com.pangu.framework.socket.utils.AnnotationUtils;
import com.pangu.framework.socket.utils.bytecode.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommandRegister {

    private static final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    private final ConcurrentHashMap<Command, MethodProcessor> commands = new ConcurrentHashMap<>();

    public static List<MethodDefine> toMethodDefine(Class<?> clz) {
        SocketModule socketModule = AnnotationUtils.findAnnotation(clz, SocketModule.class);
        Method[] methods = clz.getMethods();
        List<MethodDefine> defines = new ArrayList<>(methods.length);
        if (socketModule == null) {
            return Collections.emptyList();
        }
        int module = socketModule.value();
        for (Method method : methods) {
            Method annotationMethod = AnnotationUtils.findAnnotationMethod(method, SocketCommand.class);
            if (annotationMethod == null) {
                continue;
            }
            SocketCommand socketCommand = annotationMethod.getAnnotation(SocketCommand.class);
            if (socketCommand == null) {
                continue;
            }
            int command = socketCommand.value();
            Raw raw = socketCommand.raw();
            MethodDefine commandDefine = initMethodCommandDefine(annotationMethod, module, command, raw, method);

            defines.add(commandDefine);
        }
        return defines;
    }

    static MethodDefine initMethodCommandDefine(Method method, int module, int command, Raw raw, Method relMethod) {
        Parameter[] parameters = method.getParameters();
        List<com.pangu.framework.socket.handler.param.Parameter> list = new ArrayList<>(parameters.length);

        String[] parameterNames = discoverer.getParameterNames(method);
        if (parameterNames == null) {
            throw new IllegalStateException("项目编译请使用 -g 或者 -parameters参数");
        }
        boolean body = false;
        boolean inBody = false;
        int length = parameters.length;
        OUTER:
        for (int i = 0; i < length; ++i) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if (type == Session.class) {
                list.add(SessionParameter.INSTANCE);
                continue;
            }
            if (type == Message.class) {
                list.add(MessageParameter.INSTANCE);
                continue;
            }
            if (type == CompletableFuture.class) {
                list.add(FutureParameter.INSTANCE);
                continue;
            }
            if (type == Attachment.class) {
                list.add(AttachmentParameter.INSTANCE);
                continue;
            }
            Annotation[] annotations = parameter.getAnnotations();
            String parameterName = parameterNames[i];
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType == InBody.class) {
                    InBody anno = (InBody) annotation;
                    list.add(new InBodyParameter(StringUtils.defaultIfEmpty(anno.value(), parameterName),
                            parameter.getParameterizedType(), anno.required()));
                    inBody = true;
                    continue OUTER;
                }
                if (annotationType == Identity.class) {
                    Identity anno = (Identity) annotation;
                    list.add(new IdentityParameter(anno.required()));
                    continue OUTER;
                }
                if (annotationType == InSession.class) {
                    InSession anno = (InSession) annotation;
                    list.add(new InSessionParameter(StringUtils.defaultIfEmpty(anno.value(), parameterName), anno.required()));
                    continue OUTER;
                }
                if (annotationType == AttachId.class) {
                    list.add(AttachmentIdParameter.INSTANCE);
                }
            }
            list.add(new BodyParameter(parameter.getParameterizedType(), raw.request()));
            body = true;
        }
        if (body && inBody) {
            throw new IllegalArgumentException("类[" + method.getDeclaringClass().getName() + "].[" + method.getName()
                    + "]同时存在@InBody 和 BodyParameter, 请单独使用其中一种");
        }
        Parameters cusParameters = new Parameters(list.toArray(new com.pangu.framework.socket.handler.param.Parameter[0]));
        return new MethodDefine(new Command((short) module, (short) command), cusParameters, method.getGenericReturnType(), method, relMethod, raw);
    }

    public MethodProcessor getProcessor(Command command) {
        return commands.get(command);
    }

    public void register(Object object) {
        Class<?> clz = AopUtils.getTargetClass(object);
        if (clz.isInterface()) {
            log.warn("指令处理器不可注册接口[" + clz.getName() + "]");
            return;
        }
        List<MethodDefine> defines = toMethodDefine(clz);
        if (defines.isEmpty()) {
            return;
        }
        Wrapper proxyWrapper = Wrapper.getWrapper(clz);
        for (MethodDefine commandDefine : defines) {
            MethodProcessor processor = new MethodProcessor(commandDefine, object, commandDefine.getMethod(), proxyWrapper);
            MethodProcessor pre = commands.putIfAbsent(commandDefine.getCommand(), processor);
            if (pre != null) {
                throw new IllegalArgumentException("命令号[" + commandDefine.getCommand() + "]已经被[" + pre + "]注册");
            }
        }
    }
}

package com.pangu.framework.console;

import com.pangu.framework.console.impl.MethodCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Console {

    private static final Logger logger = LoggerFactory.getLogger(Console.class);

    /**
     * 控制台停止状态
     */
    private boolean stop;
    /**
     * 控制台指令集合
     */
    private final Map<String, Command> commands = new TreeMap<>();

    /**
     * 控制台管理的当前的应用上下文
     */
    private final AbstractApplicationContext applicationContext;
    /**
     * 当前应用上下文内的类型转换服务类
     */
    private final ConversionService conversionService;

    /**
     * 控制台构造方法
     *
     * @param applicationContext
     */
    public Console(AbstractApplicationContext applicationContext) {
        // 注册 JVM ShutdownHook
        applicationContext.registerShutdownHook();
        this.applicationContext = applicationContext;
        this.conversionService = applicationContext.getBean(ConversionService.class);
        registerCommand();
        commands.put(cmdStop.name(), cmdStop);
        commands.put(cmdList.name(), cmdList);
        commands.put(cmdReload.name(), cmdReload);
        if (logger.isDebugEnabled()) {
            logger.debug("控制台初始化完成");
        }
    }

    /**
     * 停止控制台的方法
     */
    public void stop() {
        applicationContext.close();
        stop = true;
        if (logger.isInfoEnabled()) {
            logger.info("控制台关闭");
        }
        logger.info("关闭JAVA进程");
        System.exit(0);
    }

    /**
     * 检查控制台是否已经停止
     *
     * @return
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * 获取指令名对应的命令对象实例
     *
     * @param name
     * @return
     */
    public Command getCommand(String name) {
        return commands.get(name);
    }

    /**
     * 启动控制台
     */
    public void start() {
        ConsoleRunner runner = new ConsoleRunner(this);
        Thread thread = new Thread(runner, "控制台输入线程");
        thread.start();
        if (logger.isInfoEnabled()) {
            logger.info("控制台启动");
        }
    }

    /**
     * 命令方法过滤器
     */
    private ReflectionUtils.MethodFilter findCommand = method -> method.getAnnotation(ConsoleCommand.class) != null;

    /**
     * 注册控制台命令
     */
    private void registerCommand() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ConsoleBean.class);
        for (Entry<String, Object> entry : beans.entrySet()) {
            final Object bean = entry.getValue();
            ReflectionUtils.doWithMethods(bean.getClass(), method -> {
                Command command = new MethodCommand(bean, method, conversionService);
                commands.put(command.name(), command);
            }, findCommand);
        }
    }

    /**
     * 停止控制台的指令
     */
    private Command cmdStop = new Command() {
        @Override
        public String name() {
            return "stop";
        }

        @Override
        public void execute(String[] arguments) {
            stop();
        }

        @Override
        public String description() {
            return "停止控制台";
        }
    };
    /**
     * 列出全部控制台指令的指令
     */
    private Command cmdList = new Command() {
        @Override
        public String name() {
            return "list";
        }

        @Override
        public void execute(String[] arguments) {
            for (Entry<String, Command> entry : commands.entrySet()) {
                Command command = entry.getValue();
                System.out.println(command.name() + "\t:\t" + command.description());
            }
        }

        @Override
        public String description() {
            return "列出全部控制台指令";
        }
    };

    /**
     * 重载所有Command指令,需要配合JRebel使用,JRebel虽然支持热更新类,但是热更新后新增的指令并没有注册到commands集合,执行一遍registerCommand便可热更新Command
     */
    private Command cmdReload = new Command() {
        @Override
        public String name() {
            return "reload";
        }

        @Override
        public void execute(String[] arguments) {
            String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
            if (beanDefinitionNames.length > 0) {
                String beanName = beanDefinitionNames[0];
                //防报错
                if ("org.springframework.aop.config.internalAutoProxyCreator".equals(beanName)) {
                    applicationContext.getBeanFactory().getBeanDefinition(beanName);
                }
            }
            registerCommand();
        }

        @Override
        public String description() {
            return "刷新指令";
        }
    };

}

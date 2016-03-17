package net.isger.brick.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.core.Console;
import net.isger.brick.core.ConsoleManager;
import net.isger.brick.core.Module;
import net.isger.brick.inject.Container;
import net.isger.brick.inject.ContainerBuilder;
import net.isger.brick.inject.ContainerProvider;
import net.isger.brick.plugin.UIPluginModule;
import net.isger.brick.ui.UIDesigner;
import net.isger.util.Asserts;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Brick容器监听器
 * 
 * @author issing
 *
 */
@Ignore
public class BrickListener implements ServletContextListener {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(BrickListener.class);
    }

    /**
     * 初始上下文
     */
    public void contextInitialized(ServletContextEvent event) {
        /* 初始容器 */
        this.initial(event.getServletContext());
    }

    /**
     * 初始
     * 
     * @param context
     */
    private synchronized void initial(ServletContext context) {
        Asserts.state(
                context.getAttribute(Constants.BRICK_WEB_MANAGER) == null,
                "Cannot initialize console because there is already a Brick console manager present - %s",
                "check whether you have multiple definitions in your web.xml");
        context.log("Initializing Brick Console Manager");
        long startTime = System.currentTimeMillis();
        /* 创建管理器 */
        ConsoleManager manager = createConsoleManager(context);
        context.setAttribute(Constants.BRICK_WEB_MANAGER, manager);
        /* 加载容器 */
        manager.load();
        if (LOG.isDebugEnabled()) {
            LOG.info("Initialization completed in {} ms",
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 创建管理器
     * 
     * @param context
     * @return
     */
    protected ConsoleManager createConsoleManager(ServletContext context) {
        /* 获取配置管理器 */
        Class<?> managerClass = getManagerClass(context);
        Asserts.isAssignable(ConsoleManager.class, managerClass,
                "Custom console manager [%s] is not of [%s]", managerClass,
                ConsoleManager.class);
        ConsoleManager manager = (ConsoleManager) Reflects
                .newInstance(managerClass);
        /* 添加供应容器 */
        addContainerProviders(manager, getWebName(context));
        return manager;
    }

    /**
     * 获取管理器类
     * 
     * @param context
     * @return
     */
    private Class<?> getManagerClass(ServletContext context) {
        return Reflects.getClass(Strings.empty(
                context.getInitParameter(Constants.BRICK_WEB_MANAGER),
                ConsoleManager.class.getName()), Reflects.getClassLoader());
    }

    /**
     * 获取网名
     * 
     * @param context
     * @return
     */
    private static String getWebName(ServletContext context) {
        return Strings.empty(
                context.getInitParameter(Constants.BRICK_WEB_NAME), Strings
                        .empty(context.getContextPath().replaceAll("[/\\\\]+",
                                ""), Constants.DEFAULT));
    }

    /**
     * 添加供应容器
     * 
     * @param manager
     */
    private void addContainerProviders(ConsoleManager manager,
            final String webName) {
        manager.getContainerProviders();
        manager.addContainerProvider(new ContainerProvider() {
            public void register(ContainerBuilder builder) {
                builder.constant(Constants.BRICK_WEB_NAME, webName);
                builder.factory(WebCommand.class, webName);
                builder.factory(Module.class, Constants.MOD_PLUGIN,
                        UIPluginModule.class);
                builder.factory(UIDesigner.class, Constants.MOD_PLUGIN);
            }

            public boolean isReload() {
                return false;
            }
        });
    }

    /**
     * 获取控制台
     * 
     * @param context
     * @return
     */
    public static Console getConsole(ServletContext context) {
        Object manager = context.getAttribute(Constants.BRICK_WEB_MANAGER);
        Asserts.isInstanceOf(ConsoleManager.class, manager,
                "The brick console manager is not properly initialized");
        return ((ConsoleManager) manager).getConsole();
    }

    /**
     * 制造命令
     * 
     * @param request
     * @param response
     * @return
     */
    public static WebCommand makeCommand(HttpServletRequest request,
            HttpServletResponse response) {
        ServletContext context = request.getSession().getServletContext();
        Container container = getConsole(context).getContainer();
        /* 获取网名 */
        String webName = container.getInstance(String.class,
                Constants.BRICK_WEB_NAME);
        /* 获取命令 */
        WebCommand command = container.getInstance(WebCommand.class, webName);
        command.initial(request, response);
        return command;
    }

    /**
     * 注销上下文
     */
    public void contextDestroyed(ServletContextEvent event) {
        destroy(event.getServletContext());
    }

    /**
     * 注销
     * 
     * @param context
     */
    private synchronized void destroy(ServletContext context) {
        Console console = getConsole(context);
        if (console != null) {
            console.destroy();
        }
        context.removeAttribute(Constants.BRICK_WEB_MANAGER);
    }

}

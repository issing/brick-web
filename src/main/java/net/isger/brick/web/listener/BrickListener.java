package net.isger.brick.web.listener;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.core.ConsoleManager;
import net.isger.brick.core.Module;
import net.isger.brick.inject.ContainerBuilder;
import net.isger.brick.inject.ContainerProvider;
import net.isger.brick.plugin.UIPluginModule;
import net.isger.brick.ui.UIDesigner;
import net.isger.brick.util.WebHelpers;
import net.isger.brick.web.WebCommand;
import net.isger.brick.web.WebConstants;
import net.isger.util.Asserts;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;

/**
 * 容器监听器
 * 
 * @author issing
 *
 */
@Ignore
public class BrickListener implements ServletContextListener {

    private static final String WEBSOCKET_CONTAINER = "javax.websocket.server.ServerContainer";

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(BrickListener.class);
    }

    /**
     * 初始上下文
     */
    public final void contextInitialized(ServletContextEvent event) {
        /* 初始容器 */
        initial(event.getServletContext());
    }

    /**
     * 初始
     * 
     * @param context
     */
    protected synchronized void initial(ServletContext context) {
        Asserts.throwState(WebHelpers.getManager(context) == null, "Cannot initialize console because there is already a Brick console manager present - %s", "check whether you have multiple definitions in your web.xml");
        context.log("Initializing Brick Console Manager");
        long startTime = System.currentTimeMillis();
        /* 创建管理器 */
        ConsoleManager manager = createConsoleManager(context);
        WebHelpers.setManager(context, manager);
        /* 加载容器 */
        manager.load();
        if (LOG.isDebugEnabled()) {
            LOG.info("Initialization completed in {} ms", System.currentTimeMillis() - startTime);
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
        Asserts.isAssignable(ConsoleManager.class, managerClass, "Custom console manager [%s] is not of [%s]", managerClass, ConsoleManager.class);
        ConsoleManager manager = (ConsoleManager) Reflects.newInstance(managerClass);
        /* 添加供应容器 */
        addContainerProviders(manager, context);
        return manager;
    }

    /**
     * 获取管理器类
     * 
     * @param context
     * @return
     */
    private Class<?> getManagerClass(ServletContext context) {
        return Reflects.getClass(Strings.empty(context.getInitParameter(WebConstants.BRICK_WEB_MANAGER), ConsoleManager.class.getName()), Reflects.getClassLoader());
    }

    /**
     * 添加供应容器
     * 
     * @param manager
     */
    @SuppressWarnings("unchecked")
    private void addContainerProviders(ConsoleManager manager, ServletContext context) {
        final String webName = getWebName(context);
        final String webPath = new File(context.getRealPath("./")).getAbsolutePath();
        final Object webSocket = context.getAttribute(WEBSOCKET_CONTAINER);
        context.setAttribute(WebConstants.BRICK_WEB_NAME, webName);
        context.setAttribute(WebConstants.BRICK_WEB_PATH, webPath);
        manager.addContainerProvider(new ContainerProvider() {
            public void register(ContainerBuilder builder) {
                builder.factory(WebCommand.class, WebConstants.WEB);
                builder.factory(Module.class, WebConstants.MOD_PLUGIN, UIPluginModule.class);
                builder.factory(UIDesigner.class, WebConstants.MOD_PLUGIN);
                if (webSocket != null) {
                    builder.constant((Class<Object>) Reflects.getClass(WEBSOCKET_CONTAINER), Constants.SYSTEM, webSocket);
                }
            }

            public boolean isReload() {
                return Helpers.toBoolean(context.getInitParameter(WebConstants.BRICK_WEB_RELOAD));
            }
        });
    }

    /**
     * 获取网名
     * 
     * @param context
     * @return
     */
    public static String getWebName(ServletContext context) {
        return Strings.empty(context.getInitParameter(WebConstants.BRICK_WEB_NAME), Strings.empty(context.getContextPath().replaceAll("[/\\\\]+", ""), WebConstants.DEFAULT));
    }

    /**
     * 注销上下文
     */
    public final void contextDestroyed(ServletContextEvent event) {
        destroy(event.getServletContext());
    }

    /**
     * 注销
     * 
     * @param context
     */
    protected synchronized void destroy(ServletContext context) {
        Console console = WebHelpers.getConsole(context);
        if (console != null) {
            console.destroy();
        }
        context.removeAttribute(WebConstants.BRICK_WEB_MANAGER);
    }

}

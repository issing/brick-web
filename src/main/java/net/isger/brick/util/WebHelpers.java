package net.isger.brick.util;

import javax.servlet.ServletContext;

import net.isger.brick.core.Console;
import net.isger.brick.core.ConsoleManager;
import net.isger.brick.web.WebConstants;

public class WebHelpers {

    private WebHelpers() {
    }

    /**
     * 获取控制台管理器
     * 
     * @param context
     * @return
     */
    public static ConsoleManager getManager(ServletContext context) {
        return (ConsoleManager) context.getAttribute(WebConstants.BRICK_WEB_MANAGER);
    }

    /**
     * 设置控制管理器
     * 
     * @param context
     * @param manager
     */
    public static void setManager(ServletContext context, ConsoleManager manager) {
        context.setAttribute(WebConstants.BRICK_WEB_MANAGER, manager);
    }

    /**
     * 设置控制台
     * 
     * @param context
     * @param console
     */
    public static void setConsole(ServletContext context, Console console) {
        context.setAttribute(WebConstants.BRICK_WEB_CONSOLE, console);
    }

    /**
     * 获取控制台
     * 
     * @param context
     * @return
     */
    public static Console getConsole(ServletContext context) {
        Console console = (Console) context.getAttribute(WebConstants.BRICK_WEB_CONSOLE);
        if (console == null) {
            ConsoleManager manager = getManager(context);
            if (manager != null) setConsole(context, console = manager.getConsole());
        }
        return console;
    }

}

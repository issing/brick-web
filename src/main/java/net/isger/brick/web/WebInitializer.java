package net.isger.brick.web;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Context;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.util.WebHelpers;
import net.isger.util.Files;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class WebInitializer implements ServletContainerInitializer {

    @Ignore(mode = Mode.INCLUDE)
    @Alias(WebConstants.WEB)
    protected Context context;

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    protected Console console;

    public void onStartup(Set<Class<?>> c, ServletContext context) throws ServletException {
        String webPath = Files.getFile(context.getRealPath("./")).getAbsolutePath();
        context.log(Strings.format("Binded context [%s] to [%s]", Strings.empty(this.context.getPath(), "/"), webPath));
        context.setAttribute(Constants.BRICK_ENCODING, this.context.findParameter(WebConstants.BRICK_ENCODING));
        context.setAttribute(WebConstants.BRICK_WEB_NAME, this.context.findParameter(WebConstants.BRICK_WEB_NAME));
        context.setAttribute(WebConstants.BRICK_WEB_VIEW, this.context.findParameter(WebConstants.BRICK_WEB_VIEW));
        context.setAttribute(WebConstants.BRICK_WEB_PATH, webPath);
        WebHelpers.setConsole(context, this.console);
    }

}

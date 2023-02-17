package net.isger.brick.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.auth.AuthCommand;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.ui.Screen;
import net.isger.brick.util.WebHelpers;
import net.isger.brick.web.view.Viewers;
import net.isger.util.Helpers;
import net.isger.util.Strings;

public class WebFilter implements Filter {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(WebFilter.class);
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        if (!WebCommand.isAction(request)) {
            chain.doFilter(request, response);
            return;
        }
        /* 生成命令 */
        BaseCommand cmd;
        try {
            cmd = WebCommand.makeCommand(request, response);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("(!) Invalid request [{}]", request.getRequestURI(), e);
            }
            Viewers.render("failure", e, request, response);
            return;
        }
        /* 执行命令 */
        ServletContext context = request.getSession().getServletContext();
        try {
            WebHelpers.getConsole(context).execute(cmd);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("(!) Failure to process [{}]", WebCommand.getDomain(cmd), WebCommand.getName(cmd), e);
            }
            Viewers.render("failure", e, request, response);
            return;
        }
        String view = Strings.empty(context.getAttribute(WebConstants.BRICK_WEB_VIEW));
        Object result = cmd.getResult();
        /* 授权访问 */
        if (cmd instanceof AuthCommand) {
            if (!Helpers.toBoolean(result)) {
                view = "unauth";
            }
            result = ((BaseCommand) ((AuthCommand) cmd).getToken()).getResult();
        }
        /* 界面导向 */
        Screen screen;
        String name;
        if (result instanceof Screen) {
            screen = (Screen) result;
            if (screen.see("@stream") != null) {
                name = "stream";
            } else {
                name = (String) screen.see("@name");
                if (name == null) {
                    name = "plain";
                    result = screen.see("result");
                } else {
                    name = Strings.empty(name, view); // 空字符串替换为默认值
                }
            }
            Viewers.render(name, result, request, response);
        }
    }

}

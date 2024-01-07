package net.isger.brick.web;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.auth.AuthToken;
import net.isger.brick.core.BaseCommand;

/**
 * 网络身份
 * 
 * @author issing
 */
public class WebIdentity extends AuthIdentity {

    private HttpServletRequest request;

    private HttpSession session;

    protected WebIdentity() {
    }

    public WebIdentity(HttpServletRequest request) {
        this(null, request);
    }

    public WebIdentity(AuthToken<?> token, HttpServletRequest request) {
        super(request.getSession().getId(), token);
        this.initial(null, request);
    }

    protected void initial(WebCommand command, HttpServletRequest request) {
        this.request = request;
        this.active(true);
    }

    public Object getAttribute(String name) {
        return this.session.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            this.session.removeAttribute(name);
        } else {
            this.session.setAttribute(name, value);
        }
    }

    public void active(boolean createable) {
        super.active(createable);
        this.session = this.request.getSession(createable);
        synchronized (this.session) {
            this.session.setAttribute(BaseCommand.CTRL_IDENTITY, this);
        }
    }

    public void setTimeout(int timeout) {
        this.session.setMaxInactiveInterval(timeout);
    }

    public void clear() {
        super.clear();
        Enumeration<?> es = this.session.getAttributeNames();
        while (es.hasMoreElements()) {
            this.session.removeAttribute((String) es.nextElement());
        }
        this.session.setAttribute(BaseCommand.CTRL_IDENTITY, this);
    }

    /**
     * 取出身份
     * 
     * @param request
     * @return
     */
    public static WebIdentity obtain(HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (session) {
            WebIdentity identity = (WebIdentity) session.getAttribute(BaseCommand.CTRL_IDENTITY);
            if (identity == null) identity = new WebIdentity(request);
            return identity;
        }
    }

}

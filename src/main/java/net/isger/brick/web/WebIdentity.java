package net.isger.brick.web;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.auth.AuthToken;
import net.isger.brick.core.BaseCommand;

public class WebIdentity extends AuthIdentity {

    private HttpServletRequest request;

    private HttpSession session;

    public WebIdentity(HttpServletRequest request) {
        this(null, request);
    }

    public WebIdentity(AuthToken<?> token, HttpServletRequest request) {
        super(token);
        this.request = request;
        active(true);
    }

    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            session.removeAttribute(name);
        } else {
            session.setAttribute(name, value);
        }
    }

    public void active(boolean create) {
        super.active(create);
        session = request.getSession(create);
        session.setAttribute(BaseCommand.CTRL_IDENTITY, this);
    }

    public void setTimeout(int timeout) {
        session.setMaxInactiveInterval(timeout);
    }

    public void clear() {
        super.clear();
        Enumeration<?> es = session.getAttributeNames();
        while (es.hasMoreElements()) {
            session.removeAttribute((String) es.nextElement());
        }
        session.setAttribute(BaseCommand.CTRL_IDENTITY, this);
    }

    public static WebIdentity take(HttpServletRequest request) {
        HttpSession session = request.getSession();
        WebIdentity identity = (WebIdentity) session.getAttribute(BaseCommand.CTRL_IDENTITY);
        if (identity == null) {
            identity = new WebIdentity(request);
        }
        return identity;
    }

}

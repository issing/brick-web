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
        this.active(true);
        this.session.setAttribute(BaseCommand.CTRL_IDENTITY, this);
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
        this.session = request.getSession(create);
    }

    public void clear() {
        super.clear();
        Enumeration<?> es = this.session.getAttributeNames();
        while (es.hasMoreElements()) {
            this.session.removeAttribute((String) es.nextElement());
        }
        this.session.setAttribute(BaseCommand.CTRL_IDENTITY, this);
    }

    public static WebIdentity take(HttpServletRequest request) {
        HttpSession session = request.getSession();
        WebIdentity identity = (WebIdentity) session
                .getAttribute(BaseCommand.CTRL_IDENTITY);
        if (identity == null) {
            identity = new WebIdentity(request);
        }
        return identity;
    }

}

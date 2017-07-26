package net.isger.brick.web;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.auth.AuthToken;

public class WebIdentity extends AuthIdentity {

    private HttpSession session;

    public WebIdentity(HttpSession session) {
        this(null, session);
    }

    public WebIdentity(AuthToken<?> token, HttpSession session) {
        super(token);
        this.session = session;
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

    public void clear() {
        super.clear();
        Enumeration<?> es = session.getAttributeNames();
        while (es.hasMoreElements()) {
            session.removeAttribute((String) es.nextElement());
        }
    }

}

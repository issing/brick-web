package net.isger.brick.web.tunnel;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.brick.inject.Container;
import net.isger.brick.util.WebHelpers;
import net.isger.brick.web.WebConstants;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 网页套接字配置器
 * 
 * @author issing
 */
public class WebSocketConfigurator extends Configurator {

    /** 核心容器 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Container container;

    public final void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        super.modifyHandshake(sec, request, response);
        sec.getUserProperties().put(HandshakeRequest.class.getName(), request);
        Console console = WebSocketConfigurator.getConsole(request);
        console.getContainer().inject(this);
    }

    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (!this.container.contains(endpointClass, WebConstants.WEB)) ConstantStrategy.set(this.container, endpointClass, WebConstants.WEB, super.getEndpointInstance(endpointClass));
        return this.container.getInstance(endpointClass, WebConstants.WEB);
    }

    public static HandshakeRequest getRequest(EndpointConfig config) {
        return getRequest(config.getUserProperties());
    }

    static HandshakeRequest getRequest(Map<String, Object> properties) {
        return (HandshakeRequest) properties.get(HandshakeRequest.class.getName());
    }

    public static Console getConsole(EndpointConfig config) {
        return getConsole(getRequest(config));
    }

    static Console getConsole(Map<String, Object> properties) {
        return getConsole(getRequest(properties));
    }

    public static Console getConsole(HandshakeRequest request) {
        HttpSession session = (HttpSession) request.getHttpSession();
        return WebHelpers.getConsole(session.getServletContext());
    }

    public static Object getProperty(Session session, String key) {
        return session.getUserProperties().get(key);
    }

    public static void setProperty(Session session, String key, Object value) {
        session.getUserProperties().put(key, value);
    }

}
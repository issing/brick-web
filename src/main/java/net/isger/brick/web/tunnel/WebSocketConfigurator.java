package net.isger.brick.web.tunnel;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

public class WebSocketConfigurator extends Configurator {

    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        sec.getUserProperties().put(HttpServletRequest.class.getName(), request);
    }

    public static HttpServletRequest getRequest(EndpointConfig config) {
        return (HttpServletRequest) config.getUserProperties().get(HttpServletRequest.class.getName());
    }

}
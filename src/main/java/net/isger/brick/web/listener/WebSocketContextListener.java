package net.isger.brick.web.listener;

import java.util.Map.Entry;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.web.tunnel.WebSocketTunnel;
import net.isger.brick.web.tunnel.WebSocketTunnelConversion;
import net.isger.util.Asserts;
import net.isger.util.reflect.Converter;

public class WebSocketContextListener implements LifecycleListener {

    private static final Logger LOG;

    private WebSocketTunnel tunnel;

    static {
        LOG = LoggerFactory.getLogger(WebSocketContextListener.class);
        Converter.addConversion(WebSocketTunnelConversion.getInstance());
    }

    public void lifecycleEvent(LifecycleEvent event) {
        if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
            if (event.getSource() instanceof StandardContext) {
                StandardContext context = (StandardContext) event.getSource();
                ServerContainer serverContainer = (ServerContainer) context.getServletContext().getAttribute(ServerContainer.class.getName());
                Asserts.throwState(serverContainer != null, "No implementation class found for %s", ServerContainer.class.getName());
                /* 注册端点 */
                if (this.tunnel != null) {
                    for (Entry<String, ServerEndpointConfig> entry : this.tunnel.gets().entrySet()) {
                        ServerEndpointConfig config = entry.getValue();
                        try {
                            if (LOG.isDebugEnabled()) LOG.info("Deployment [{}] WebSocket [{}]", config.getPath(), config.getEndpointClass().getName());
                            serverContainer.addEndpoint(config); // 该配置优先级低于注解方式（可能不生效）
                        } catch (DeploymentException e) {
                            throw Asserts.state(e.getMessage(), e.getCause());
                        }
                    }
                }
            }
        }
    }

}

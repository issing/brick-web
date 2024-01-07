package net.isger.brick.web.tunnel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.util.Files;
import net.isger.util.Helpers;
import net.isger.util.Strings;

/**
 * 网络套接字隧道
 * 
 * @author issing
 */
public class WebSocketTunnel {

    private static final Logger LOG;

    private Map<String, ServerEndpointConfig> configs;

    static {
        LOG = LoggerFactory.getLogger(WebSocketTunnel.class);
    }

    public WebSocketTunnel() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public WebSocketTunnel(List<Object> resources) {
        this.configs = new HashMap<String, ServerEndpointConfig>();
        if (resources != null) {
            for (Object resource : resources) {
                if (resource instanceof Map) {
                    for (Entry<String, Object> entry : ((Map<String, Object>) resource).entrySet()) {
                        if (isServerEnpoint(resource = entry.getValue())) add(makeEndpointConfig(entry.getKey(), resource));
                    }
                } else if (isServerEnpoint(resource)) add(makeEndpointConfig(resource));
            }
        }
    }

    public void add(ServerEndpointConfig config) {
        String path = config.getPath();
        if (LOG.isDebugEnabled()) LOG.info("Binding [{}] tunnel [{}]", path, config);
        if ((config = configs.put(path, config)) != null) LOG.warn("(!) Discard [{}] tunnel [{}]", path, config);
    }

    public ServerEndpointConfig get(String path) {
        return this.configs.get(path);
    }

    public Map<String, ServerEndpointConfig> gets() {
        return Collections.unmodifiableMap(this.configs);
    }

    /**
     * 生成端点配置
     * 
     * @param endpoint
     * @return
     */
    private ServerEndpointConfig makeEndpointConfig(Object endpoint) {
        return makeEndpointConfig(null, endpoint);
    }

    /**
     * 生成端点配置
     * 
     * @param path
     * @param endpoint
     * @return
     */
    @SuppressWarnings("unchecked")
    private ServerEndpointConfig makeEndpointConfig(String path, final Object endpoint) {
        Class<?> endpointClass = endpoint.getClass();
        if (Strings.isEmpty(path)) {
            ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
            path = Strings.empty(annotation == null ? null : annotation.value(), Files.toPath(getName(endpointClass)));
        }
        return ServerEndpointConfig.Builder.create(endpointClass, path).configurator(new WebSocketConfigurator() {
            public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                return (T) endpoint; // 单例模式
            }
        }).build();
    }

    public static final String getName(Class<?> clazz) {
        return getName(clazz, "");
    }

    public static final String getName(Class<?> clazz, String name) {
        return Helpers.getAliasName(clazz, "Tunnel$", Strings.toLower(name));
    }

    public static boolean isServerEnpoint(Object instance) {
        return instance != null && (instance instanceof Class ? (Class<?>) instance : instance.getClass()).getAnnotation(ServerEndpoint.class) != null;
    }

}

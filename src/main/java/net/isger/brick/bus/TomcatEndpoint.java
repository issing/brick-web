package net.isger.brick.bus;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.filters.CorsFilter;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.Airfone;
import net.isger.brick.core.Console;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.brick.util.anno.Digest;
import net.isger.brick.util.anno.Digest.Stage;
import net.isger.brick.web.WebConstants;
import net.isger.brick.web.WebFilter;
import net.isger.brick.web.WebInitializer;
import net.isger.util.Asserts;
import net.isger.util.Files;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class TomcatEndpoint extends SocketEndpoint {

    private static final Logger LOG;

    private transient Tomcat tomcat;

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    protected Console console;

    @Alias(WebConstants.BRICK_ENCODING)
    @Ignore(mode = Mode.INCLUDE)
    private Charset encoding;

    private String baseDir;

    private String path;

    private String view;

    private String cors;

    private TomcatSSLConfig ssl;

    private boolean reload;

    private WebInitializer initializer;

    private WebFilter filter;

    private List<LifecycleListener> lifecycles;

    private transient Airfone airfone;

    static {
        LOG = LoggerFactory.getLogger(TomcatEndpoint.class);
    }

    public TomcatEndpoint() {
        this.tomcat = new Tomcat();
        this.path = "";
        this.view = "json";
    }

    @Digest(stage = Stage.INITIAL)
    protected void install() {
        this.console.addAirfone(this.airfone = new Airfone() {
            public boolean ack(int action) {
                if (action == Airfone.ACTION_DESTROY) {
                    while (!isActive()) Helpers.sleep(200l);
                    TomcatEndpoint.this.tomcat.getServer().await();
                }
                return true;
            }
        });
    }

    /**
     * 打开服务端口
     */
    protected final void open() {
        super.open();
        if (Strings.isEmpty(this.baseDir)) this.baseDir = Files.toPath(this.console.getContainer().getInstance(String.class, Constants.BRICK_PATH), ".brick/tomcat");
        this.path = Strings.replaceIgnoreCase(this.path, "[/\\\\]+$");
        /* 绑定服务端口 */
        InetSocketAddress address = getAddress();
        this.tomcat.setHostname(address.getHostName());
        this.tomcat.setBaseDir(this.baseDir);
        try {
            Connector connector = new Connector();
            connector.setPort(address.getPort());
            this.tomcat.getService().addConnector(connector);
            if (this.ssl != null) this.tomcat.getService().addConnector(this.ssl.getConnector());
            this.tomcat.setConnector(connector);
            this.tomcat.getHost().setAutoDeploy(this.reload);
            File workPath = Files.getFile(this.baseDir, "webapps");
            if (!workPath.exists()) workPath.mkdirs();
            File docPath = new File(workPath, name());
            if (!docPath.exists()) docPath.mkdirs();
            while (!this.console.hasReady()) Helpers.sleep(200l); // 等待控制台就绪
            this.bind(this.tomcat.addWebapp(this.path, docPath.getAbsolutePath()));
            LOG.info("Listening [{}]", address);
            this.tomcat.start();
        } catch (Exception e) {
            console.remove(airfone);
            throw Asserts.state("Failure to bind [%s]", address, e);
        }
    }

    protected void bind(Context context) {
        /* 容器初始化 */
        ConstantStrategy.set(this.container, Context.class, WebConstants.WEB, context);
        context.addParameter(WebConstants.BRICK_WEB_NAME, name());
        context.addParameter(WebConstants.BRICK_WEB_VIEW, this.view);
        context.addParameter(Constants.BRICK_ENCODING, this.encoding.name());
        for (Entry<String, Object> parameter : getParameters().entrySet()) context.addParameter(parameter.getKey(), Strings.empty(parameter.getValue()));
        context.addServletContainerInitializer(this.initializer == null ? this.container.inject(this.initializer = new WebInitializer()) : this.initializer, null);
        if (this.lifecycles != null) for (LifecycleListener lifecycle : this.lifecycles) context.addLifecycleListener(lifecycle);
        /* 内置跨域过滤器 */
        if (Strings.isNotEmpty(this.cors)) {
            FilterDef corsDefine = new FilterDef();
            corsDefine.setFilterName("cors");
            corsDefine.setFilter(this.container.inject(new CorsFilter()));
            corsDefine.addInitParameter(CorsFilter.PARAM_CORS_ALLOWED_ORIGINS, this.cors);
            FilterMap corsMapper = new FilterMap();
            corsMapper.setFilterName("cors");
            corsMapper.addURLPattern("/*");
            corsMapper.setCharset(this.encoding);
            context.addFilterDef(corsDefine);
            context.addFilterMap(corsMapper);
        }
        /* 内置服务过滤器 */
        FilterDef brickDefine = new FilterDef();
        brickDefine.setFilterName(Constants.BRICK);
        brickDefine.setFilter(this.filter == null ? this.container.inject(this.filter = new WebFilter()) : this.filter);
        FilterMap brickMapper = new FilterMap();
        brickMapper.setFilterName(Constants.BRICK);
        brickMapper.addURLPattern("/*");
        brickMapper.setCharset(this.encoding);
        context.addFilterDef(brickDefine);
        context.addFilterMap(brickMapper);
    }

    protected void close() {
        try {
            this.tomcat.stop();
        } catch (LifecycleException e) {
            LOG.info("(!) Failure to close tomcat", e);
        }
        super.close();
    }
}

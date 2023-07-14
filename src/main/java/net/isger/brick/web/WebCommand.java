package net.isger.brick.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import net.isger.brick.Constants;
import net.isger.brick.auth.AuthCommand;
import net.isger.brick.auth.AuthHelper;
import net.isger.brick.auth.AuthModule;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Console;
import net.isger.brick.core.GateCommand;
import net.isger.brick.inject.Container;
import net.isger.brick.ui.UICommand;
import net.isger.brick.ui.UIConstants;
import net.isger.brick.util.WebHelpers;
import net.isger.util.Files;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class WebCommand extends UICommand implements WebConfig {

    public static final String BRICK_WEB_PREFIX = "brick-web:";

    private static final String REGEX_MOBILE_DEVICE = ".*(android|webos|ios|iphone|ipod|blackberry|phone).*";

    private static final String KEY_AUTH_DOMAIN = "brick.auth.domain";

    private static final String ENCODING = "ISO-8859-1";

    private static final String INDEX = "index";

    private HttpServletRequest request;

    private HttpServletResponse response;

    @Alias(WebConstants.BRICK_ENCODING)
    @Ignore(mode = Mode.INCLUDE)
    private Charset encoding;

    /**
     * 请求检测
     * 
     * @param request
     * @return
     */
    public static boolean isAction(HttpServletRequest request) {
        return !request.getRequestURI().contains(".");
    }

    /**
     * 访问命令（根据应用配置情况做认证包装）
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public static BaseCommand makeCommand(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return makeCommand(request, response, null);
    }

    /**
     * 访问命令（根据应用配置情况做认证包装）
     * 
     * @param request
     * @param response
     * @param parameters
     * @return
     * @throws Exception
     */
    public static BaseCommand makeCommand(HttpServletRequest request, HttpServletResponse response, Map<String, Object> parameters) throws Exception {
        request.setAttribute(WebConstants.KEY_MOBILE, request.getHeader("user-agent").toLowerCase().matches(REGEX_MOBILE_DEVICE));
        ServletContext context = request.getSession().getServletContext();
        GateCommand token = makeWebCommand(request, response, parameters);
        String domain = null;
        synchronized (context) {
            if ((domain = (String) context.getAttribute(KEY_AUTH_DOMAIN)) == null) {
                Console console = WebHelpers.getConsole(context);
                AuthModule authModule = (AuthModule) console.getModule(Constants.MOD_AUTH);
                if (authModule.getGate(domain = token.getDomain()) == null) {
                    if (authModule.getGate(domain = console.getModuleName(token)) == null) {
                        domain = "";
                    }
                }
                context.setAttribute(KEY_AUTH_DOMAIN, domain);
            }
        }
        /* 制作认证命令 */
        if (Strings.isNotEmpty(domain)) {
            AuthCommand cmd = AuthHelper.makeCommand(token.getIdentity(), domain, token);
            cmd.setOperate(AuthCommand.OPERATE_CHECK);
            token = cmd;
        }
        return token;
    }

    /**
     * 访问命令
     * 
     * @param request
     * @param response
     * @param parameters
     * @return
     * @throws Exception
     */
    private static WebCommand makeWebCommand(HttpServletRequest request, HttpServletResponse response, Map<String, Object> parameters) throws Exception {
        ServletContext context = request.getSession().getServletContext();
        Container container = WebHelpers.getConsole(context).getContainer();
        /* 初始命令 */
        WebCommand command = container.getInstance(WebCommand.class, WebConstants.WEB);
        command.initial(request, response, parameters);
        /* 权限会话 */
        command.setIdentity(WebIdentity.take(request));
        return command;
    }

    /**
     * 初始命令
     * 
     * @param request
     * @param response
     * @param parameters
     * @throws Exception
     */
    void initial(HttpServletRequest request, HttpServletResponse response, Map<String, Object> parameters) throws Exception {
        this.request = request;
        this.response = response;
        makeTarget();
        makeParameters(parameters);
        try {
            setPayload(new String(Files.read(request.getInputStream()), encoding));
        } catch (IOException e) {
        }
    }

    public int getMemoryThreshold() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getMaxFileSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getMaxRequestSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getEncoding() {
        return this.encoding.name();
    }

    /**
     * 生成目标
     * 
     * @param request
     * @return
     */
    protected void makeTarget() {
        String domain;
        ServletContext context = request.getServletContext();
        String webName = Strings.empty(context.getAttribute(WebConstants.BRICK_WEB_NAME), context.getServletContextName());
        String contextPath = request.getContextPath().replaceAll("[/\\\\]+", "/");
        String path = request.getRequestURI().replaceAll("[/\\\\]+", "/");
        String[] pending = path.split("[:]");
        if (pending.length > 1) {
            domain = (contextPath.length() > 0 ? pending[0].substring(contextPath.length()) : pending[0]).replaceFirst("[/]", "");
            path = Strings.join(false, pending, 1);
        } else if (Strings.isEmpty(contextPath)) {
            domain = webName;
        } else {
            if (WebConstants.DEFAULT.equals(webName)) {
                domain = contextPath.replaceFirst("[/]", "");
            } else {
                domain = webName;
            }
            path = path.substring(contextPath.length());
        }
        this.setDomain(domain);

        pending = (String[]) Helpers.newArray(path.split("!"), 2);
        this.setName(Strings.empty(pending[0].replaceFirst("/", "").replaceAll("[/]", "."), INDEX));
        this.setOperate(Strings.empty(pending[1], UIConstants.OPERATE_SCREEN));
    }

    /**
     * 生成参数
     * 
     * @param parameters
     * @throws Exception
     */
    protected void makeParameters(Map<String, Object> parameters) throws Exception {
        String charset = "GET".equalsIgnoreCase(request.getMethod()) ? ENCODING : Strings.empty(request.getCharacterEncoding(), encoding.name());
        /* 获取请求参数 */
        Map<String, Object> result = new HashMap<String, Object>();
        if (parameters == null) {
            Enumeration<?> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                result.put(name, toEncoding(charset, request.getParameterValues(name)));
            }
            if (ServletFileUpload.isMultipartContent(request)) {
                result.putAll(WebMultipart.parse(this, request));
            }
        } else {
            Object value;
            for (Entry<String, Object> param : parameters.entrySet()) {
                value = param.getValue();
                if (value instanceof String[]) {
                    value = toEncoding(charset, (String[]) value);
                }
                result.put(param.getKey(), Helpers.compact(value));
            }
        }
        setParameter(result);
    }

    private Object toEncoding(String charset, String... values) {
        int count = values.length;
        if (!encoding.name().equalsIgnoreCase(charset)) {
            for (int i = 0; i < count; i++) {
                values[i] = ENCODING.equalsIgnoreCase(charset) ? values[i] : newString(charset, values[i]);
            }
        }
        return count == 1 ? values[0] : values;
    }

    private String newString(String charset, String value) {
        try {
            return new String(value.getBytes(charset), encoding);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getHeader(CharSequence key) {
        if (String.valueOf(key).startsWith(BRICK_WEB_PREFIX)) {
            if ((BRICK_WEB_PREFIX + "request").equals(key)) {
                return (T) request;
            } else if ((BRICK_WEB_PREFIX + "response").equals(key)) {
                return (T) response;
            } else if ((BRICK_WEB_PREFIX + "remoteHost").equals(key)) {
                return (T) request.getRemoteHost();
            }
        }
        return super.getHeader(key);
    }

}

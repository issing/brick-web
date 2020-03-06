package net.isger.brick.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import net.isger.brick.ui.UICommand;
import net.isger.brick.ui.UIConstants;
import net.isger.util.Asserts;
import net.isger.util.Files;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class WebCommand extends UICommand {

    public static final String BRICK_WEB_PREFIX = "brick-web:";

    private static final String NAME_INDEX = "index";

    private HttpServletRequest request;

    private HttpServletResponse response;

    @Alias(WebConstants.BRICK_WEB_NAME)
    @Ignore(mode = Mode.INCLUDE)
    private String webName;

    @Alias(WebConstants.BRICK_ENCODING)
    @Ignore(mode = Mode.INCLUDE)
    private Charset encoding;

    /**
     * 初始命令
     * 
     * @param request
     * @param response
     * @param parameters
     */
    void initial(HttpServletRequest request, HttpServletResponse response, Map<String, Object> parameters) {
        this.request = request;
        this.response = response;
        makeTarget();
        makeParameters(parameters);
        try {
            setPayload(new String(Files.read(request.getInputStream()), encoding));
        } catch (IOException e) {
        }
    }

    /**
     * 生成目标
     * 
     * @param request
     * @return
     */
    protected void makeTarget() {
        String domain;
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
        this.setName(Strings.empty(pending[0].replaceFirst("/", "").replaceAll("[/]", "."), NAME_INDEX));
        this.setOperate(Strings.empty(pending[1], UIConstants.OPERATE_SCREEN));
    }

    /**
     * 生成参数
     * 
     * @param parameters
     */
    protected void makeParameters(Map<String, Object> parameters) {
        String charset = request.getCharacterEncoding();
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            charset = "ISO-8859-1";
        }
        /* 获取请求参数 */
        Map<String, Object> result = new HashMap<String, Object>();
        if (parameters == null) {
            Enumeration<?> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                result.put(name, toEncoding(charset, request.getParameterValues(name)));
            }
            if (ServletFileUpload.isMultipartContent(request)) {
                throw Asserts.state("Unimplements multipart process");
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
                values[i] = "ISO-8859-1".equalsIgnoreCase(charset) ? values[i] : newString(charset, values[i]);
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

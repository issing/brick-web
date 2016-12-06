package net.isger.brick.web;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.isger.brick.ui.UICommand;
import net.isger.brick.ui.UIConstants;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class WebCommand extends UICommand {

    private static final String NAME_INDEX = "index";

    private HttpServletRequest request;

    private HttpSession session;

    @Alias(WebConstants.BRICK_WEB_NAME)
    @Ignore(mode = Mode.INCLUDE)
    private String webName;

    /**
     * 初始命令
     * 
     * @param request
     * @param response
     */
    void initial(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.session = request.getSession();
        makeTarget();
        makeParameters();
        this.setIdentity(session.getId());
    }

    /**
     * 生成目标
     * 
     * @param request
     * @return
     */
    protected void makeTarget() {
        this.setDomain(webName);
        String contextPath = request.getContextPath().replaceAll("[/\\\\]+",
                "/");
        String path = request.getRequestURI().replaceAll("[/\\\\]+", "/");
        if (Strings.isNotEmpty(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String[] target = (String[]) Helpers.getArray(path.split("!"), 2);
        this.setName(Strings.empty(
                target[0].replaceFirst("/", "").replaceAll("[/]", "."),
                NAME_INDEX));
        this.setOperate(Strings.empty(target[1], UIConstants.OPERATE_SCREEN));
    }

    /**
     * 生成参数
     */
    protected void makeParameters() {
        String charset = request.getCharacterEncoding();
        /* 获取请求参数 */
        Map<String, Object> result = new HashMap<String, Object>();
        String name;
        Enumeration<?> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            name = (String) names.nextElement();
            result.put(name,
                    toEncoding(charset, request.getParameterValues(name)));
        }
        setParameter(result);
    }

    private Object toEncoding(String charset, String... values) {
        int count = values.length;
        if (count == 1) {
            try {
                return new String(values[0].getBytes("ISO-8859-1"), charset);
            } catch (UnsupportedEncodingException e) {
                return values[0];
            }
        }
        for (int i = 0; i < count; i++) {
            values[i] = (String) toEncoding(charset, values[i]);
        }
        return values;
    }
}

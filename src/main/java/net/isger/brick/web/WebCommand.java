package net.isger.brick.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.ui.UICommand;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class WebCommand extends UICommand {

    private static final String SCREEN_INDEX = "index";

    private static final String OPERATE_SCREEN = "screen";

    private HttpServletRequest request;

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
        makeTarget();
        makeParameters();
    }

    /**
     * 制造目标
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
        this.setScreen(Strings.empty(target[0].replaceFirst("/", "")
                .replaceAll("[/]", "."), SCREEN_INDEX));
        this.setOperate(Strings.empty(target[1], OPERATE_SCREEN));
    }

    /**
     * 制造参数
     */
    protected void makeParameters() {
        /* 获取请求参数 */
        Map<String, Object> result = new HashMap<String, Object>();
        String name;
        String[] values;
        Enumeration<?> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            name = (String) names.nextElement();
            values = request.getParameterValues(name);
            if (values != null && values.length == 1) {
                result.put(name, values[0]);
            } else {
                result.put(name, values);
            }
        }
        setParameter(result);
    }

}

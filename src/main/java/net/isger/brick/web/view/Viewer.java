package net.isger.brick.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.util.Named;

/**
 * 视图器接口
 * 
 * @author issing
 */
public interface Viewer extends Named {

    /**
     * 屏显渲染
     * 
     * @param content
     * @param response
     * @param request
     */
    public void render(Object content, HttpServletRequest request, HttpServletResponse response);

}

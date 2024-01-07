package net.isger.brick.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.ui.Screen;
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
     * @param screen
     * @param response
     * @param request
     */
    public void render(Screen screen, HttpServletRequest request, HttpServletResponse response);

}

package net.isger.brick.web.view;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.Constants;
import net.isger.brick.ui.Screen;
import net.isger.util.Strings;

public class PlainViewer extends AbstractViewer {

    public void render(Screen screen, HttpServletRequest request, HttpServletResponse response) {
        if (screen != null) {
            response.setContentType("text/plain; charset=" + Strings.empty(request.getSession().getServletContext().getAttribute(Constants.BRICK_ENCODING), Constants.ENC_UTF8));
            try {
                response.getWriter().print(screen.see("result"));
            } catch (IOException e) {
            }
        }
    }

}

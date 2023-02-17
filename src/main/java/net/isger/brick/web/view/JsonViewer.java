package net.isger.brick.web.view;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.Constants;
import net.isger.util.Helpers;
import net.isger.util.Strings;

public class JsonViewer extends AbstractViewer {

    public void render(Object content, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=" + Strings.empty(request.getSession().getServletContext().getAttribute(Constants.BRICK_ENCODING), Constants.ENC_UTF8));
        try {
            response.getWriter().print(Helpers.toJson(content));
        } catch (IOException e) {
        }
    }

}

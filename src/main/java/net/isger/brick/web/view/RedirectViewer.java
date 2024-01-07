package net.isger.brick.web.view;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.ui.Screen;
import net.isger.util.Asserts;
import net.isger.util.Strings;

public class RedirectViewer extends AbstractViewer {

    public void render(Screen screen, HttpServletRequest request, HttpServletResponse response) {
        String location = getLocation(screen);
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            throw Asserts.state("Failure to redirect [%s]", location, e);
        }
    }

    /**
     * 路径
     *
     * @return
     */
    public String getLocation(Screen screen) {
        return Strings.empty(screen.see("@location"));
    }

}

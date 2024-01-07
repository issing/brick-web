package net.isger.brick.web.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.ui.Screen;
import net.isger.util.hitch.Director;

public class Viewers {

    /** 视图器属性键 */
    private static final String KEY_VIEWERS = "brick.web.viewers";

    /** 视图器默认路径 */
    private static final String VIEWERS_PATH = "net/isger/brick/web/view";

    private static final Logger LOG;

    private static final Viewers INSTANCE;

    private Map<String, Viewer> viewers;

    static {
        LOG = LoggerFactory.getLogger(Viewers.class);
        INSTANCE = new Viewers();
        new Director() {
            protected String directPath() {
                return directPath(KEY_VIEWERS, VIEWERS_PATH);
            }
        }.direct(INSTANCE);
    }

    private Viewers() {
        viewers = new HashMap<String, Viewer>();
    }

    public static void addViewer(Viewer viewer) {
        String name = viewer.name();
        if (LOG.isDebugEnabled()) LOG.info("Achieve viewer [{}]", name);
        viewer = INSTANCE.viewers.put(name, viewer);
        if (viewer != null && LOG.isDebugEnabled()) LOG.warn("(!) Discard viewer [{}]", viewer);
    }

    public static Viewer getViewer(String name) {
        return INSTANCE.viewers.get(name);
    }

    public static void render(String name, Screen screen, HttpServletRequest request, HttpServletResponse response) {
        Viewer viewer = getViewer(name);
        if (viewer != null) viewer.render(screen, request, response);
    }

}

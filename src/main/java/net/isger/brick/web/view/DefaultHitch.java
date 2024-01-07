package net.isger.brick.web.view;

/**
 * 默认便车
 * 
 * @author issing
 */
public class DefaultHitch {

    /**
     * 免费便车
     * 
     * @param source
     */
    public static void hitch(Object source) {
        // 亲，要看好巴士，别搭错车（^o^）
        if (!(source instanceof Viewers)) {
            return;
        }
        Viewers.addViewer(new PlainViewer());
        Viewers.addViewer(new JsonViewer());
        Viewers.addViewer(new RedirectViewer());
    }

}

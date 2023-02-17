package net.isger.brick.web.view;

import net.isger.util.Helpers;
import net.isger.util.Strings;

/**
 * 抽象视图器
 * 
 * @author issing
 */
public abstract class AbstractViewer implements Viewer {

    /** 视图器名称 */
    private String name;

    public String name() {
        if (Strings.isEmpty(name)) {
            name = Helpers.getAliasName(this.getClass(), "Viewer$");
        }
        return name;
    }

}

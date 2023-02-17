package net.isger.brick.test.screen;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.ui.BaseScreen;

/**
 * 主界面
 * 
 * @author issing
 */
public class IndexScreen extends BaseScreen {

    /**
     * 默认入口
     * 
     * @param cmd
     */
    public void screen(PluginCommand cmd) {
        this.message = "this is index.";
    }

}

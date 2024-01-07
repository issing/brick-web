package net.isger.brick.web;

import net.isger.brick.ui.BaseScreen;
import net.isger.brick.ui.Screen;

/**
 * 网页异常
 * 
 * @author issing
 */
public class WebFailure {

    private WebFailure() {
    }

    public static Screen newScreen(final Throwable cause) {
        return new BaseScreen() {
            {
                this.direct("failure", cause);
            }
        };
    }

}

package net.isger.brick.web;

import net.isger.brick.inject.ContainerBuilder;
import net.isger.brick.ui.UIProvider;

public class WebProvider extends UIProvider {

    public void register(ContainerBuilder builder) {
        super.register(builder);
        builder.factory(WebCommand.class, WebConstants.WEB);
    }

}

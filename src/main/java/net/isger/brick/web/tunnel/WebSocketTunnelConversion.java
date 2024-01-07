package net.isger.brick.web.tunnel;

import java.io.File;
import java.lang.reflect.Type;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.conversion.Conversion;
import net.isger.util.scan.ScanFilter;

public class WebSocketTunnelConversion extends ScanLoader implements Conversion {

    private static final ScanFilter FILTER;

    private static WebSocketTunnelConversion INSTANCE;

    static {
        FILTER = new ScanFilter() {
            public boolean isDeep(File root, File path) {
                return true;
            }

            public boolean accept(String name) {
                return Strings.endWithIgnoreCase(name, "Tunnel[.]class");
            }
        };
    }

    private WebSocketTunnelConversion() {
        super(WebSocketTunnel.class, FILTER);
    }

    public static WebSocketTunnelConversion getInstance() {
        if (INSTANCE == null) INSTANCE = new WebSocketTunnelConversion();
        return INSTANCE;
    }

    public boolean isSupport(Type type) {
        return WebSocketTunnel.class.isAssignableFrom(Reflects.getRawClass(type));
    }

    public Object convert(Type type, Object res, ClassAssembler assembler) {
        return new WebSocketTunnel(toList(load(res, assembler)));
    }

    public String toString() {
        return WebSocketTunnel.class.getName();
    }

}
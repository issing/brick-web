package net.isger.brick.bus;

import org.apache.catalina.connector.Connector;

import net.isger.util.Strings;

/**
 * 
 * 
 * @author issing
 */
public class TomcatSSLConfig {

    private int port;

    private String protocol;

    private int maxThreads;

    private boolean clientAuth;

    private String keyAlias;

    private String keystoreType;

    private String keystoreFile;

    private String keystorePass;

    public TomcatSSLConfig() {
        this.protocol = "HTTP/1.1";
        this.port = 443;
        this.maxThreads = 200;
    }

    public Connector getConnector() {
        Connector connector = new Connector(this.protocol);
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(this.port);
        if (Strings.isNotEmpty(this.keyAlias)) {
            connector.setProperty("keyAlias", this.keyAlias);
        }
        connector.setProperty("keystoreType", this.keystoreType);
        connector.setProperty("keystoreFile", this.keystoreFile);
        if (Strings.isNotEmpty(this.keystorePass)) {
            connector.setProperty("keystorePass", this.keystorePass);
        }
        connector.setProperty("clientAuth", Boolean.toString(this.clientAuth));
        connector.setProperty("sslProtocol", "TLS");
        if (maxThreads > 0) {
            connector.setProperty("maxThreads", String.valueOf(this.maxThreads));
        }
        connector.setProperty("SSLEnabled", "true");
        return connector;
    }

}

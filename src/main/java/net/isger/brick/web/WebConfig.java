package net.isger.brick.web;

public interface WebConfig {

    public int getMemoryThreshold();

    public long getMaxFileSize();

    public long getMaxRequestSize();

    public String getEncoding();

}

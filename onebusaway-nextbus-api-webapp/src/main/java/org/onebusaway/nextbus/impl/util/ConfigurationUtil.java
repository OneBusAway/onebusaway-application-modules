package org.onebusaway.nextbus.impl.util;

public class ConfigurationUtil {
  
  private String transiTimeHost = "localhost";
  private String transiTimePort = "8080";
  private String transiTimeKey = "8a3273b0";
  
  public ConfigurationUtil(){}
  
  public String getTransiTimeHost() {
    return transiTimeHost;
  }

  public void setTransiTimeHost(String transiTimeHost) {
    this.transiTimeHost = transiTimeHost;
  }

  public String getTransiTimePort() {
    return transiTimePort;
  }

  public void setTransiTimePort(String transitTimePort) {
    this.transiTimePort = transitTimePort;
  }

  public String getTransiTimeKey() {
    return transiTimeKey;
  }

  public void setTransiTimeKey(String transiTimeKey) {
    this.transiTimeKey = transiTimeKey;
  }

  
}

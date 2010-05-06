package org.onebusaway.tcip.services;

public interface TcipServletConfig {
  public String getSourceApp();
  public String getSourceIp();
  public int getSourcePort();
  public XmlSerializationStrategy getXmlSerializationStrategy();
}

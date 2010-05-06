package org.onebusaway.tcip.services;

public interface XmlSerializationStrategy {
  public String toXml(Object object);
  public Object fromXml(String xml);
}

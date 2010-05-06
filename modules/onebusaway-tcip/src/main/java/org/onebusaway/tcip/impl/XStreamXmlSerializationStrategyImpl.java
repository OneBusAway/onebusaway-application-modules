package org.onebusaway.tcip.impl;

import org.onebusaway.tcip.model.PISchedAdherenceCountdown;
import org.onebusaway.tcip.model.PiStopPointETA;
import org.onebusaway.tcip.model.PiStopPointETASub;
import org.onebusaway.tcip.model.TcipMessage;
import org.onebusaway.tcip.services.XmlSerializationStrategy;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XStreamXmlSerializationStrategyImpl implements
    XmlSerializationStrategy {

  private XStream _xstream;

  public XStreamXmlSerializationStrategyImpl() {
    QNameMap nameMap = new QNameMap();
    nameMap.setDefaultNamespace("http://www.TCIP-Final-3-0-3");
    StaxDriver staxDriver = new StaxDriver(nameMap);
    _xstream = new XStream(staxDriver);
    
    _xstream.processAnnotations(TcipMessage.class);
    _xstream.processAnnotations(PiStopPointETASub.class);
    _xstream.processAnnotations(PiStopPointETA.class);
    _xstream.processAnnotations(PISchedAdherenceCountdown.class);
  }

  public Object fromXml(String xml) {
    return _xstream.fromXML(xml);
  }

  public String toXml(Object object) {
    String value = _xstream.toXML(object);
    if( value.startsWith("<?"))
      value = value.replaceFirst("^<\\?.*\\?>","");
    return value;
  }

}

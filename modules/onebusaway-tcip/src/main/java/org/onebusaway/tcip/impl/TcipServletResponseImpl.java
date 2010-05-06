package org.onebusaway.tcip.impl;

import org.onebusaway.tcip.model.TcipMessage;
import org.onebusaway.tcip.services.TcipServletConfig;
import org.onebusaway.tcip.services.TcipServletResponse;
import org.onebusaway.tcip.services.XmlSerializationStrategy;

import org.apache.mina.core.session.IoSession;

import java.util.Date;

class TcipServletResponseImpl implements TcipServletResponse {

  private IoSession _session;

  private TcipServletConfig _config;

  public TcipServletResponseImpl(IoSession session, TcipServletConfig context) {
    _session = session;
    _config = context;
  }

  public void writeMessage(TcipMessage message) {

    if (message.getCreated() == null)
      message.setCreated(new Date());
    if (message.getSourceapp() == null)
      message.setSourceapp(_config.getSourceApp());
    if (message.getSourceip() == null)
      message.setSourceip(_config.getSourceIp());
    if (message.getSourceport() == 0)
      message.setSourceport(_config.getSourcePort());
    if( message.getSchVersion() == null)
      message.setSchVersion("TCIP 3.0.3");
    if (message.getNoNameSpaceSchemaLocation() == null)
      message.setNoNameSpaceSchemaLocation("TCIP_Final_3_0_3.xsd");

    XmlSerializationStrategy serialization = _config.getXmlSerializationStrategy();
    String xmlString = serialization.toXml(message);

    _session.write(xmlString + "\n");
  }

  public void close() {
    _session.close(false);
  }
}

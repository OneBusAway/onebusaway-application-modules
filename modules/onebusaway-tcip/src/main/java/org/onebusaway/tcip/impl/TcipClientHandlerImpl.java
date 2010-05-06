package org.onebusaway.tcip.impl;

import org.onebusaway.tcip.model.TcipMessage;
import org.onebusaway.tcip.services.TcipServlet;
import org.onebusaway.tcip.services.TcipServletConfig;
import org.onebusaway.tcip.services.TcipServletResponse;
import org.onebusaway.tcip.services.XmlSerializationStrategy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.Map;

public class TcipClientHandlerImpl extends IoHandlerAdapter {

  private XmlSerializationStrategy _serialization = new XStreamXmlSerializationStrategyImpl();

  private Map<Class<?>, TcipServlet> _servletMapping;

  private TcipServletConfig _config;
  
  public TcipClientHandlerImpl(TcipServletConfig config) {
    _config = config;
  }
  
  public TcipServletResponse createResponse(IoSession session) {
    return new TcipServletResponseImpl(session,_config);
  }

  public void setServletMapping(Map<Class<?>, TcipServlet> servletMapping) {
    _servletMapping = servletMapping;
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause)
      throws Exception {
    
  }

  @Override
  public void messageReceived(IoSession session, Object message)
      throws Exception {

    String messageAsString = message.toString();
    
    if( messageAsString.length() == 0)
      return;
    
    Object object = _serialization.fromXml(messageAsString);
    if (!(object instanceof TcipMessage)) {
      throw new IllegalArgumentException("not a message: " + object);
    }

    TcipMessage tcipMessage = (TcipMessage) object;
    Class<? extends TcipMessage> requestType = tcipMessage.getClass();
    TcipServlet servlet = _servletMapping.get(requestType);

    TcipServletRequestImpl request = new TcipServletRequestImpl();
    request.setMessage(tcipMessage);
    TcipServletResponse response = createResponse(session);
    servlet.service(request, response);
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status)
      throws Exception {
    System.out.println("IDLE " + session.getIdleCount(status));
  }
}

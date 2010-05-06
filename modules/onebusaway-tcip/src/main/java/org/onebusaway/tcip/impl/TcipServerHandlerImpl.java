package org.onebusaway.tcip.impl;

import org.onebusaway.tcip.model.TcipMessage;
import org.onebusaway.tcip.services.TcipServlet;
import org.onebusaway.tcip.services.TcipServletConfig;
import org.onebusaway.tcip.services.XmlSerializationStrategy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.Map;

public class TcipServerHandlerImpl extends IoHandlerAdapter {

  private Map<Class<?>, TcipServlet> _servletMapping;
  private TcipServletConfig _config;

  public TcipServerHandlerImpl(TcipServletConfig config) {
    _config = config;
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
    
    System.out.println("server receive: " + message);
    
    XmlSerializationStrategy serialization = _config.getXmlSerializationStrategy();
    Object object = serialization.fromXml(messageAsString);
    if (!(object instanceof TcipMessage)) {
      throw new IllegalArgumentException("not a message: " + object);
    }

    TcipMessage tcipMessage = (TcipMessage) object;
    Class<? extends TcipMessage> requestType = tcipMessage.getClass();
    TcipServlet servlet = _servletMapping.get(requestType);

    TcipServletRequestImpl request = new TcipServletRequestImpl();
    request.setMessage(tcipMessage);
    TcipServletResponseImpl response = new TcipServletResponseImpl(session,
        _config);
    
    servlet.service(request, response);
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status)
      throws Exception {
    System.out.println("IDLE " + session.getIdleCount(status));
  }

}

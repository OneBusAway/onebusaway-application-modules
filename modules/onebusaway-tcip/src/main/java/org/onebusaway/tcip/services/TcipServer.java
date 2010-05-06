package org.onebusaway.tcip.services;

import org.onebusaway.tcip.impl.TcipServerHandlerImpl;
import org.onebusaway.tcip.impl.XStreamXmlSerializationStrategyImpl;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TcipServer {

  private static final int PORT = 8080;

  private XmlSerializationStrategy _serialization = new XStreamXmlSerializationStrategyImpl();

  private List<TcipServletMapping> _servletMappings;

  private int _port = PORT;

  private IoAcceptor _acceptor;

  private String _sourceApp = "0";

  private String _sourceIp;

  public void setPort(int port) {
    _port = port;
  }

  public void setSourceApp(String sourceApp) {
    _sourceApp = sourceApp;
  }

  public void setServletMappings(List<TcipServletMapping> servletMappings) {
    _servletMappings = servletMappings;
  }

  public void start() throws IOException {

    InetSocketAddress address = new InetSocketAddress(_port);
    _sourceIp = address.getAddress().getHostAddress();
    _acceptor = new NioSocketAcceptor();
    //_acceptor.getFilterChain().addLast("logger", new LoggingFilter());
    ProtocolCodecFilter filter = new ProtocolCodecFilter(
        new TextLineCodecFactory(Charset.forName("UTF-8")));
    _acceptor.getFilterChain().addLast("code", filter);

    TcipServerHandlerImpl handler = new TcipServerHandlerImpl(new TcipServerConfigImpl());

    Map<Class<?>, TcipServlet> mappings = new HashMap<Class<?>, TcipServlet>();
    for (TcipServletMapping mapping : _servletMappings)
      mappings.put(mapping.getMessageType(), mapping.getServlet());
    handler.setServletMapping(mappings);

    _acceptor.setHandler(handler);

    _acceptor.getSessionConfig().setReadBufferSize(2048);
    _acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
    _acceptor.bind(address);
  }

  public void stop() {
    _acceptor.unbind();
  }

  private class TcipServerConfigImpl implements TcipServletConfig {

    public String getSourceApp() {
      return _sourceApp;
    }

    public String getSourceIp() {
      return _sourceIp;
    }

    public int getSourcePort() {
      return _port;
    }

    public XmlSerializationStrategy getXmlSerializationStrategy() {
      return _serialization;
    }
  }
}

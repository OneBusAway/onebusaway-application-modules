package org.onebusaway.tcip.services;

import org.onebusaway.tcip.impl.TcipClientHandlerImpl;
import org.onebusaway.tcip.impl.XStreamXmlSerializationStrategyImpl;
import org.onebusaway.tcip.model.TcipMessage;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TcipClient {

  private final Logger _log = LoggerFactory.getLogger(TcipClient.class);

  private XmlSerializationStrategy _serialization = new XStreamXmlSerializationStrategyImpl();

  private ConnectionHandler _connectionHandler = new ConnectionHandler();

  private TcipClientHandlerImpl _handler = new TcipClientHandlerImpl(
      new TcipServletConfigImpl());

  private String _hostname;

  private int _port;

  private TcipServletResponse _response;

  private InetSocketAddress _localAddress;

  private boolean _automaticReconnect = true;

  private List<TcipServletMapping> _servletMappings = new ArrayList<TcipServletMapping>();

  private List<TcipFutureListener<TcipClient>> _connectionListeners = new ArrayList<TcipFutureListener<TcipClient>>();

  private ExecutorService _executor;

  private Future<?> _connectionHandlerTask;

  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  public void setPort(int port) {
    _port = port;
  }

  public void setAutomaticReconnection(boolean automaticReconnect) {
    _automaticReconnect = automaticReconnect;
  }

  public void addServletMapping(TcipServletMapping mapping) {
    _servletMappings.add(mapping);
  }

  public void addConnectionListener(TcipFutureListener<TcipClient> listener) {
    _connectionListeners.add(listener);
  }

  public void start() throws Throwable {

    _executor = Executors.newSingleThreadExecutor();
    _connectionHandlerTask = _executor.submit(_connectionHandler);
  }

  public void stop() throws InterruptedException {
    _connectionHandlerTask.cancel(true);
    _executor.shutdown();
    _executor.awaitTermination(30, TimeUnit.SECONDS);
  }

  public void writeMessage(TcipMessage message) {
    _response.writeMessage(message);
  }

  /****
   * Private Methods
   ****/

  private class TcipServletConfigImpl implements TcipServletConfig {

    public String getSourceApp() {
      return null;
    }

    public String getSourceIp() {
      return _localAddress.getHostName();
    }

    public int getSourcePort() {
      return _localAddress.getPort();
    }

    public XmlSerializationStrategy getXmlSerializationStrategy() {
      return _serialization;
    }
  }

  private class ConnectionHandler implements Runnable {

    public synchronized void run() {

      IoConnector connector = createConnector();

      InetSocketAddress address = new InetSocketAddress(_hostname, _port);
      ConnectFuture connectionAttempt = null;

      try {

        while (!Thread.currentThread().isInterrupted()) {

          _log.info("Attempting to connect");
          connectionAttempt = connector.connect(address);
          connectionAttempt.await();

          if (connectionAttempt.isConnected()) {

            initiateSessionAndWaitForDisconnect(connectionAttempt);

            if (!_automaticReconnect)
              break;

          } else {

            _log.info("Connection failed");

            // We're not connected... do we support automatic reconnection?
            if (!_automaticReconnect)
              throw new IllegalStateException(connectionAttempt.getException());

            Thread.sleep(30 * 1000);
          }
        }
      } catch (InterruptedException ex) {

      } finally {
        if (connectionAttempt != null)
          connectionAttempt.cancel();
        if (_response != null)
          _response.close();
        connector.dispose();
      }
    }

    private IoConnector createConnector() {

      IoConnector connector = new NioSocketConnector();

      LoggingFilter logger = new LoggingFilter();
      logger.setExceptionCaughtLogLevel(LogLevel.ERROR);
      logger.setMessageReceivedLogLevel(LogLevel.DEBUG);
      logger.setMessageSentLoglevel(LogLevel.DEBUG);
      logger.setSessionClosedLoglevel(LogLevel.DEBUG);
      logger.setSessionCreatedLoglevel(LogLevel.DEBUG);
      logger.setSessionIdleLoglevel(LogLevel.DEBUG);
      logger.setSessionOpenedLoglevel(LogLevel.DEBUG);

      connector.getFilterChain().addLast("logger", logger);
      TextLineCodecFactory factory = new TextLineCodecFactory(
          Charset.forName("UTF-8"));
      factory.setDecoderMaxLineLength(1024 * 100);
      ProtocolCodecFilter filter = new ProtocolCodecFilter(factory);
      connector.getFilterChain().addLast("code", filter);

      Map<Class<?>, TcipServlet> mappings = new HashMap<Class<?>, TcipServlet>();
      for (TcipServletMapping mapping : _servletMappings)
        mappings.put(mapping.getMessageType(), mapping.getServlet());
      _handler.setServletMapping(mappings);

      connector.setHandler(_handler);

      return connector;
    }

    private void initiateSessionAndWaitForDisconnect(ConnectFuture future)
        throws InterruptedException {

      IoSession session = future.getSession();
      _localAddress = (InetSocketAddress) session.getLocalAddress();
      _response = _handler.createResponse(session);

      for (TcipFutureListener<TcipClient> listener : _connectionListeners)
        listener.operationCompleted(TcipClient.this);

      while (!Thread.currentThread().isInterrupted()) {

        Thread.sleep(30 * 1000);

        if (!future.isConnected())
          return;
      }
    }
  }
}

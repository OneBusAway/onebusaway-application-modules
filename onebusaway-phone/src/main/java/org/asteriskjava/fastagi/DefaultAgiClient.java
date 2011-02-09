package org.asteriskjava.fastagi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.asteriskjava.fastagi.internal.AgiClientConnection;
import org.asteriskjava.util.SocketConnectionFacade;
import org.asteriskjava.util.internal.CustomSocketConnectionFacadeImpl;

public class DefaultAgiClient {

  private String host;

  /**
   * The port to listen on.
   */
  private int port = 8000;

  private AgiClientScript script;

  private Map<String, String> _parameters = new HashMap<String, String>();

  public DefaultAgiClient() {

  }

  /**
   * Creates a new DefaultAgiServer.
   */
  public DefaultAgiClient(String host, int port, AgiClientScript clientScript) {
    setHost(host);
    setPort(port);
    setScript(clientScript);
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setScript(AgiClientScript script) {
    this.script = script;
  }

  /****
   * AGI Request Paremters
   ****/

  public void setCallerId(String callerId) {
    setParameter("agi_callerid", callerId);
  }

  public void setNetworkScript(String networkScript) {
    setParameter("agi_network_script", networkScript);
  }

  public void setParameter(String name, String value) {
    _parameters.put(name, value);
  }
  
  public String removeParameter(String name) {
    return _parameters.remove(name);
  }

  protected SocketConnectionFacade createClientSocket() throws IOException {
    return new CustomSocketConnectionFacadeImpl(host, port, false, 0, 0,
        CustomSocketConnectionFacadeImpl.NL_PATTERN);
  }

  public Future<?> run() throws IOException {
    SocketConnectionFacade socket = createClientSocket();
    AgiClientConnection connection = new AgiClientConnection(socket);
    return connection.run(script, _parameters);
  }

}

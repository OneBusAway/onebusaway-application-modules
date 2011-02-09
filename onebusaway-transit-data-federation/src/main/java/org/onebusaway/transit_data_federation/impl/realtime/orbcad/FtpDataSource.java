package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

public class FtpDataSource {
  private String servername;
  private String username;
  private String password;
  private int port = 21;

  public String getServername() {
    return servername;
  }

  public void setServername(String servername) {
    this.servername = servername;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

}

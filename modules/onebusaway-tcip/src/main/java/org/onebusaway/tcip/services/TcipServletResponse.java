package org.onebusaway.tcip.services;

import org.onebusaway.tcip.model.TcipMessage;

public interface TcipServletResponse {

  public void writeMessage(TcipMessage message);

  public void close();
}

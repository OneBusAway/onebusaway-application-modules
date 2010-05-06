package org.onebusaway.tcip.impl;

import org.onebusaway.tcip.model.TcipMessage;
import org.onebusaway.tcip.services.TcipServletRequest;

class TcipServletRequestImpl implements TcipServletRequest {

  private TcipMessage _message;

  public void setMessage(TcipMessage message) {
    _message = message;
  }

  public TcipMessage getMessage() {
    return _message;
  }
}

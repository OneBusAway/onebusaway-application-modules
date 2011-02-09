package org.onebusaway.users.impl.authentication;

import org.springframework.security.AuthenticationException;

public class EveryLastLoginAuthenticationException extends AuthenticationException {

  private static final long serialVersionUID = 1L;
  
  private String _mode;
  
  public EveryLastLoginAuthenticationException(String msg, String mode) {
    super(msg);
    _mode = mode;
  }
  
  public String getMode() {
    return _mode;
  }

}

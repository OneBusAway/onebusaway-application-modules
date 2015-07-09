package org.onebusaway.enterprise.webapp.api;

public class NotAuthorizedException extends Exception {

  public NotAuthorizedException(String message) {
    super(message);
  }

  private static final long serialVersionUID = 1L;

}

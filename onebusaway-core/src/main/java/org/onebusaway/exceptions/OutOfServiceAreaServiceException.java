package org.onebusaway.exceptions;

public class OutOfServiceAreaServiceException extends ServiceAreaServiceException {

  private static final long serialVersionUID = 1L;
  
  public OutOfServiceAreaServiceException() {
      super("out of service area");
  }
}

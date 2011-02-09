package org.onebusaway.exceptions;

public class InternalErrorServiceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InternalErrorServiceException() {

  }

  public InternalErrorServiceException(String message) {
    super(message);
  }

  public InternalErrorServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalErrorServiceException(Throwable cause) {
    super(cause);
  }
}

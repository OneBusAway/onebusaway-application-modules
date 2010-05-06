package org.onebusaway.exceptions;

public class InvalidArgumentServiceException extends ServiceException {

  private static final long serialVersionUID = 1L;
  private String _fieldName;
  private String _errorMessage;

  public InvalidArgumentServiceException(String fieldName, String errorMessage) {
    super("invalidArgument: field=" + fieldName + " error=" + errorMessage);
    _fieldName = fieldName;
    _errorMessage = errorMessage;
  }
  
  public String getFieldName() {
    return _fieldName;
  }
  
  public String getErrorMessage() {
    return _errorMessage;
  }
}

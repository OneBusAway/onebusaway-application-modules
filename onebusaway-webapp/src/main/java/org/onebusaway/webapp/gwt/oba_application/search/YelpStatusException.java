package org.onebusaway.webapp.gwt.oba_application.search;

public class YelpStatusException extends YelpException {

  private static final long serialVersionUID = 1L;

  public YelpStatusException(String message, int statusCode) {
    super(message);
  }
}

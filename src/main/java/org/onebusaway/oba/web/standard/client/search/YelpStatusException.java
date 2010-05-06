package org.onebusaway.oba.web.standard.client.search;

public class YelpStatusException extends YelpException {

  private static final long serialVersionUID = 1L;

  public YelpStatusException(String message, int statusCode) {
    super(message);
  }
}

package org.onebusaway.users.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestAndResponseContext {

  private static final ThreadLocal<RequestAndResponseContext> _context = new ThreadLocal<RequestAndResponseContext>();

  public static RequestAndResponseContext getContext() {
    return _context.get();
  }

  public static void setContext(RequestAndResponseContext context) {
    _context.set(context);
  }

  public static void resetContext() {
    _context.remove();
  }

  private final HttpServletRequest _request;

  private final HttpServletResponse _response;

  public RequestAndResponseContext(HttpServletRequest request,
      HttpServletResponse response) {
    _request = request;
    _response = response;
  }

  public HttpServletRequest getRequest() {
    return _request;
  }

  public HttpServletResponse getResponse() {
    return _response;
  }
}

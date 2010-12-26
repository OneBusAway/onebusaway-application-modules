package org.onebusaway.presentation.tags;

import javax.servlet.ServletContext;

import org.apache.struts2.dispatcher.Dispatcher;
import org.onebusaway.presentation.impl.ServletLibrary;

import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

public class UrlFunction {

  private ServletContext _servletContext;

  @Inject(required = true)
  public void setServletContext(ServletContext servletContext) {
    _servletContext = servletContext;
  }

  public String getUrl(String value) {
    String path = ServletLibrary.getContextPath(_servletContext);
    if (path != null)
      value = path + value;
    return value;
  }

  public static String url(String value) {
    Container container = Dispatcher.getInstance().getContainer();
    UrlFunction function = new UrlFunction();
    container.inject(function);
    return function.getUrl(value);
  }
}

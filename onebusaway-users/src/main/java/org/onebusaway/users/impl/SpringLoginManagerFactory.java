package org.onebusaway.users.impl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.onebusaway.everylastlogin.server.LoginManager;
import org.onebusaway.everylastlogin.server.LoginManagerFactory;
import org.onebusaway.everylastlogin.server.LoginServlet;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Factory class to create a {@link LoginManager} instance from an existing
 * Spring {@link WebApplicationContext} for use in the {@link LoginServlet}.
 * Assumes the presence of a {@link LoginManager} bean in the application
 * context with the name {@code "loginManager"}.
 * 
 * @author bdferris
 */
public class SpringLoginManagerFactory implements LoginManagerFactory {

  @Override
  public LoginManager createLoginManager(ServletConfig config)
      throws ServletException {

    ServletContext servletContext = config.getServletContext();
    WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    return (LoginManager) webApplicationContext.getBean("loginManager");
  }

}

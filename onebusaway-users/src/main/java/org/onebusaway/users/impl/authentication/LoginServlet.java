package org.onebusaway.users.impl.authentication;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class LoginServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private LoginManager _loginManager;

  @Autowired
  public void setLoginManager(LoginManager loginManager) {
    _loginManager = loginManager;
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    ServletContext servletContext = config.getServletContext();
    WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

    AutowireCapableBeanFactory autowireCapableBeanFactory = webApplicationContext.getAutowireCapableBeanFactory();
    autowireCapableBeanFactory.autowireBean(this);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, resp);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, resp);
  }

  private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    _loginManager.handleAuthentication(req, resp);
  }
}

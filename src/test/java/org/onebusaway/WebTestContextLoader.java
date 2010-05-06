package org.onebusaway;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;

public class WebTestContextLoader extends AbstractContextLoader {

  protected static final Logger logger = Logger.getLogger(WebTestContextLoader.class.getName());

  public final ConfigurableApplicationContext loadContext(
      final String... locations) throws Exception {

    XmlWebApplicationContext webContext = new XmlWebApplicationContext();
    ServletContext servletContext = new MockServletContext("/WebContent",
        new FileSystemResourceLoader());

    servletContext.setAttribute(
        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
        webContext);
    webContext.setServletContext(servletContext);
    webContext.setConfigLocations(locations);
    RequestContextListener rcl = new RequestContextListener();
    ServletRequestEvent sre = new ServletRequestEvent(servletContext,
        new MockHttpServletRequest());
    rcl.requestDestroyed(sre);
    rcl.requestInitialized(sre);

    webContext.refresh();
    webContext.registerShutdownHook();

    return webContext;
  }

  protected String getResourceSuffix() {
    return "-context.xml";
  }

}

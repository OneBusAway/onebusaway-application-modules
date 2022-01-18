/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.opensymphony.xwork2.util.GlobalLocalizedTextProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Listen for context configuration for localization.  Default to 
 * original onebusaway-nyc configuration. 
 *
 */
public class LocalizationListener implements ServletContextListener {

  private static final String DEFAULT_RESOURCE = "onebusaway-nyc";
  private static Logger _log = LoggerFactory.getLogger(LocalizationListener.class);
  

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    _log.debug("context init");
    ServletContext servletContext = servletContextEvent.getServletContext();
    if (servletContext == null) return; // for testing support
    String resource = (String) servletContext.getInitParameter("obanyc.resource");
    if (resource != null) {
      _log.info("found resource override=" + resource);
      new GlobalLocalizedTextProvider().addDefaultResourceBundle(resource);
    } else {
      _log.info("did not find resource override, using default localization of " + DEFAULT_RESOURCE);
      // nothing to do
      //LocalizedTextUtil.addDefaultResourceBundle(DEFAULT_RESOURCE);
    }
    
  }


  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // TODO Auto-generated method stub
    
  }

}

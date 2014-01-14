/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.container.spring.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.web.context.ServletContextAware;

public class ServletContextAwareMetadataNamingStrategy extends
    MetadataNamingStrategy implements ServletContextAware {

  private String _servletName;

  public ServletContextAwareMetadataNamingStrategy(
      JmxAttributeSource annotationSource) {
    super(annotationSource);
  }

  @Override
  public ObjectName getObjectName(Object managedBean, String beanKey)
      throws MalformedObjectNameException {

    ObjectName objName = super.getObjectName(managedBean, beanKey);

    if (_servletName != null) {
      String canonicalName = objName.getCanonicalName();
      canonicalName += ",application=" + ObjectName.quote(_servletName);
      objName = ObjectNameManager.getInstance(canonicalName);
    }

    return objName;
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    _servletName = getContextPath(servletContext);
  }
  
  private String getContextPath(ServletContext context) {
    // Get the context path without the request.
    String contextPath = "";
    try {
      String path = context.getResource("/").getPath();
      contextPath = path.substring(0, path.lastIndexOf("/"));
      contextPath = contextPath.substring(contextPath.lastIndexOf("/"));
      if (contextPath.equals("/localhost"))
        contextPath = "";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return contextPath;
  }
}

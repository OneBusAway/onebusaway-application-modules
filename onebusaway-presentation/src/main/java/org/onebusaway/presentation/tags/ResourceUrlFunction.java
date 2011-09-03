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
package org.onebusaway.presentation.tags;

import java.util.Locale;

import javax.servlet.ServletContext;

import org.apache.struts2.dispatcher.Dispatcher;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

public class ResourceUrlFunction {

  private ResourceService _resourceService;

  @Inject(required = true)
  public void setServletContext(ServletContext servletContext) {

    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
  }

  public String getExternalUrlForResource(String resourcePath) {

    Locale locale = Locale.getDefault();
    ActionContext ctx = ActionContext.getContext();
    if (ctx != null)
      locale = ctx.getLocale();

    return _resourceService.getExternalUrlForResource(resourcePath, locale);
  }

  public static String resource(String resourcePath) {

    Dispatcher instance = Dispatcher.getInstance();
    if (instance == null)
      return null;

    Container container = instance.getContainer();
    ResourceUrlFunction function = new ResourceUrlFunction();
    container.inject(function);

    return function.getExternalUrlForResource(resourcePath);
  }
}

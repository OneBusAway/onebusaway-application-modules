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
    Dispatcher instance = Dispatcher.getInstance();
    if( instance == null)
      return null;
    Container container = instance.getContainer();
    UrlFunction function = new UrlFunction();
    container.inject(function);
    return function.getUrl(value);
  }
}

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
package org.onebusaway.presentation.tags;

import javax.servlet.ServletContext;

import org.apache.struts2.dispatcher.Dispatcher;
import org.onebusaway.presentation.impl.configuration.DefaultWebappConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

/**
 * Expose the oba configuration as a taglib for easy configuration lookups.
 * Use data-sources property overrides or java system properties to override
 * ConfigurableParameters
 *
 */
public class ConfigurationValueFunction {
  
  @Inject(required = true)
  public void setServletContext(ServletContext servletContext) {

    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }
  
  private DefaultWebappConfigurationSource _source;
  
  @Autowired
  public void setDefaultWebappConfigurationSource(DefaultWebappConfigurationSource source) {
    _source = source;
  }
  
  // eg googleMapsApiKey
  public String lookup(String key) {
    return (String) _source.getConfiguration("/").get(key);
  }
  
  public static String configValue(String key) {
    Dispatcher instance = Dispatcher.getInstance();
    if (instance == null) 
      return null;
    
    Container container = instance.getContainer();
    ConfigurationValueFunction cv = new ConfigurationValueFunction();
    container.inject(cv);
    
    return cv.lookup(key);
  }
  
}

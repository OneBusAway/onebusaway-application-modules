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
package org.onebusaway.phone;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class XWorkConfigurationFactoryBean implements FactoryBean<Configuration> {

  private ApplicationContext _context;

  private List<String> _xmlConfigurationSources = new ArrayList<String>();

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  public Class<?> getObjectType() {
    return Configuration.class;
  }

  public void setXmlConfigurationSource(String xmlConfigurationSource) {
    _xmlConfigurationSources.add(xmlConfigurationSource);
  }

  public void setXmlConfigurationSources(List<String> xmlConfigurationSources) {
    _xmlConfigurationSources.addAll(xmlConfigurationSources);
  }

  public Configuration getObject() throws Exception {

    ConfigurationManager confManager = new ConfigurationManager("XWorkConfigurationFactoryBean");

    confManager.addContainerProvider(new XWorkConfigurationProvider());

    SpringContainer springContainer = new SpringContainer();
    springContainer.setApplicationContext(_context);
    confManager.addContainerProvider(springContainer);

    for (String xmlConfigurationSource : _xmlConfigurationSources) {
      XmlConfigurationProvider xml = new XmlConfigurationProvider(
          xmlConfigurationSource, true);
      xml.setThrowExceptionOnDuplicateBeans(false);
      confManager.addContainerProvider(xml);
    }

    return confManager.getConfiguration();
  }

  public boolean isSingleton() {
    return true;
  }
}

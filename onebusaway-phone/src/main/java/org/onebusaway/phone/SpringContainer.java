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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.inject.Factory;
import com.opensymphony.xwork2.spring.SpringObjectFactory;
import com.opensymphony.xwork2.test.StubConfigurationProvider;
import com.opensymphony.xwork2.util.location.LocatableProperties;

public class SpringContainer extends StubConfigurationProvider {

  private ApplicationContext _applicationContext;

  private int _autoWireStrategy = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _applicationContext = context;
  }

  public void setAutoWireStrategy(int strategy) {
    _autoWireStrategy = strategy;
  }

  @Override
  public void register(ContainerBuilder builder, LocatableProperties props)
      throws ConfigurationException {

    // Since we're about to override...
    builder.setAllowDuplicates(true);

    builder.factory(ObjectFactory.class, new Factory<ObjectFactory>() {
      public ObjectFactory create(Context xworkContext) throws Exception {
        SpringObjectFactory f = new SpringObjectFactory();
        xworkContext.getContainer().inject(f);
        f.setApplicationContext(_applicationContext);
        f.setAutowireStrategy(_autoWireStrategy);
        return f;

      }

      @Override
      public Class<? extends ObjectFactory> type() {
        return ObjectFactory.class;
      }
    });
  }
}

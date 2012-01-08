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
package org.onebusaway.container.spring;

import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.hibernate.SessionFactory;
import org.onebusaway.container.spring.ehcache.EhCacheFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * A Spring {@link BeanFactoryPostProcessor} that can set bean dependencies to
 * adjust bean creation order. Typically, you don't need to set bean
 * dependencies directly, as Spring figures it out from bean references in your
 * application context in bean property setters. Sometimes, you need to set a
 * dependency that Spring cannot detect on its own, and you'd do this by
 * adjusting the {@code depends-on} attribute of a bean defintion. While that
 * works for most cases, we've managed to find a case where that doesn't work.
 * 
 * To give a specific example, we're creating a Hibernate {@link SessionFactory}
 * that uses an EhCache {@link CacheManager} to manage the second-level cache,
 * as defined in the {@code
 * org/onebusaway/container/application-context-caching.xml} and {@code
 * org/onebusaway/container/application-context-hibernate.xml} application
 * context config files. A number of modules import these configs and add
 * additional Hibernate entity classes and mappings. These modules would also
 * like to add EhCache second-level caches for these entity classes. These
 * caches can be created with {@link EhCacheFactoryBean}, but they need to be
 * instantiated before the {@link SessionFactory}, as the session factory will
 * query the {@link CacheManager} for cache entries on creation. To ensure that
 * these cache factories are instantiated before the session factory, and we
 * can't set the {@code depends-on} for the session factory in the config, we
 * use the {@link DependencyConfigurer} to manipulate the dependency
 * relationship directly.
 * 
 * To use {@link DependencyConfigurer}, set a "properties" property for the bean
 * definition where the each property key is a bean name and each property value
 * is a list of dependent bean names separated by commas. So for example:
 * 
 * <pre class="code">
 * <bean class="org.onebusaway.container.spring.DependencyConfigurer">
 *   <property name="properties">
 *     <props>
 *     <prop key="beanA">beanB,beanC</prop>
 *    </props>
 *   </property>
 * </bean>
 * </pre>
 * 
 * This would make the bean "beanA" depend on beans "beanB" and "beanC".
 * 
 * @author bdferris
 * 
 */
public class DependencyConfigurer extends PropertyResourceConfigurer {

  private static Logger _log = LoggerFactory.getLogger(DependencyConfigurer.class);

  private boolean _ignoreInvalidKeys = false;

  @Override
  protected void processProperties(ConfigurableListableBeanFactory beanFactory,
      Properties props) throws BeansException {

    for (Enumeration<?> names = props.propertyNames(); names.hasMoreElements();) {
      String key = (String) names.nextElement();
      try {
        processKey(beanFactory, key, props.getProperty(key));
      } catch (BeansException ex) {
        String msg = "Could not process key '" + key
            + "' in PropertyOverrideConfigurer";
        if (!_ignoreInvalidKeys) {
          throw new BeanInitializationException(msg, ex);
        }
        if (logger.isDebugEnabled()) {
          logger.debug(msg, ex);
        }
      }
    }
  }

  protected void processKey(ConfigurableListableBeanFactory beanFactory,
      String beanName, String property) {

    BeanDefinition bd = beanFactory.getBeanDefinition(beanName);

    if (bd instanceof AbstractBeanDefinition) {
      AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
      Set<String> dependsOn = new LinkedHashSet<String>();
      String[] existingDependencies = abd.getDependsOn();
      if (existingDependencies != null) {
        for (String name : existingDependencies)
          dependsOn.add(name);
      }

      String[] beanNames = property.split(",");
      for (String name : beanNames)
        dependsOn.add(name);

      abd.setDependsOn(dependsOn.toArray(new String[dependsOn.size()]));

    } else {
      _log.warn("bean definition for \""
          + beanName
          + "\" does not extend AbstractBeanDefinition, so we can't set depends-on");
    }
  }

}

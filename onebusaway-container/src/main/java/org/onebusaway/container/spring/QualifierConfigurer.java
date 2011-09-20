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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.util.StringUtils;

/**
 * 
 * @author bdferris
 */
public class QualifierConfigurer implements BeanFactoryPostProcessor {

  private static Logger _log = LoggerFactory.getLogger(QualifierConfigurer.class);

  private String _target;

  private String _type = Qualifier.class.getName();

  private String _value;

  public void setTarget(String target) {
    _target = target;
  }

  public void setType(String type) {
    _type = type;
  }

  public void setValue(String value) {
    _value = value;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {

    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(_target);

    if (beanDefinition == null) {
      _log.warn("bean definition for \"" + _target + "\" not found");
      return;
    }

    if (!(beanDefinition instanceof AbstractBeanDefinition)) {
      _log.warn("bean definition for \""
          + _target
          + "\" does not extend AbstractBeanDefinition, so we can't set depends-on");
      return;
    }

    AbstractBeanDefinition abd = (AbstractBeanDefinition) beanDefinition;
    AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(_type);

    if (StringUtils.hasLength(_value))
      qualifier.setAttribute(AutowireCandidateQualifier.VALUE_KEY, _value);

    abd.addQualifier(qualifier);
  }
}

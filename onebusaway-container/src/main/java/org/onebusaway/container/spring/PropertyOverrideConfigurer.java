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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extension of Spring's
 * {@link org.springframework.beans.factory.config.PropertyOverrideConfigurer}
 * that supports {@link System#getProperty(String)} expansion of
 * 
 * <pre class="code">${...}</pre>
 * 
 * property expressions in override values.
 * 
 * @author bdferris
 * 
 */
public class PropertyOverrideConfigurer extends
    org.springframework.beans.factory.config.PropertyOverrideConfigurer {

  private static Logger _log = LoggerFactory.getLogger(PropertyOverrideConfigurer.class);

  private static final Pattern _pattern = Pattern.compile("\\$\\{([^}]+)\\}");

  private boolean ignoreInvalidBeans = false;

  public void setIgnoreInvalidBeans(boolean ignoreInvalidBeans) {
    this.ignoreInvalidBeans = ignoreInvalidBeans;
  }

  @Override
  protected void applyPropertyValue(ConfigurableListableBeanFactory factory,
      String beanName, String property, String value) {
    if (value != null)
      value = resolveValue(value);
    if (!factory.containsBeanDefinition(beanName) && ignoreInvalidBeans)
      return;
    super.applyPropertyValue(factory, beanName, property, value);
  }

  protected String resolveValue(String value) {
    Matcher m = _pattern.matcher(value);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String property = m.group(1);
      String propertyValue = System.getProperty(property);
      if (propertyValue == null) {
        _log.warn("no such System property: " + property);
        propertyValue = "${" + property + "}";
      } else {
        propertyValue = resolveValue(propertyValue);
      }

      /**
       * Make sure we escape the '\' and '$' characters, otherwise they'll be
       * treated as group references
       */
      propertyValue = Matcher.quoteReplacement(propertyValue);

      try {
        m.appendReplacement(sb, propertyValue);
      } catch (Throwable ex) {
        _log.warn("error appending replacement: propertyName=" + property
            + " propertyValue=" + propertyValue, ex);
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

}

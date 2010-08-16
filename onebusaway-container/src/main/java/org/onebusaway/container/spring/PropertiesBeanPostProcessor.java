package org.onebusaway.container.spring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.Ordered;

/**
 * A Spring {@link BeanPostProcessor} to add additional values to
 * already-created {@link Properties} object.
 * 
 * @author bdferris
 * @see ListBeanPostProcessor
 * @see PropertiesBeanPostProcessor
 */
public class PropertiesBeanPostProcessor implements BeanPostProcessor, Ordered {

  private int _order;

  private Set<String> _targets = new HashSet<String>();

  private Properties _properties;

  public void setTarget(String target) {
    setTargets(Arrays.asList(target));
  }

  public void setTargets(List<String> targets) {
    _targets.clear();
    _targets.addAll(targets);
  }

  public void setProperties(Properties properties) {
    _properties = properties;
  }

  /***************************************************************************
   * {@link Ordered} Interface
   **************************************************************************/

  public void setOrder(int order) {
    _order = order;
  }

  public int getOrder() {
    return _order;
  }

  /***************************************************************************
   * {@link BeanPostProcessor} Interface
   **************************************************************************/

  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

  public Object postProcessAfterInitialization(Object obj, String beanName)
      throws BeansException {

    if (_properties != null && _targets.contains(beanName)) {
      if (obj instanceof Properties) {
        Properties properties = (Properties) obj;
        properties.putAll(_properties);
      } else if (obj instanceof PropertiesFactoryBean) {
        PropertiesFactoryBean factory = (PropertiesFactoryBean) obj;
        System.out.println(factory);
      }
    }

    return obj;
  }

}
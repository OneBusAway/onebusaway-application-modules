package org.onebusaway.container.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.Ordered;

import java.util.Properties;

public class PropertiesBeanPostProcessor implements BeanPostProcessor, Ordered {

  private int _order;

  private String _target;

  private Properties _properties;

  public void setTarget(String target) {
    _target = target;
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

    if (_properties != null && beanName.equals(_target)) {
      if (obj instanceof Properties) {
        Properties properties = (Properties) obj;
        properties.putAll(_properties);
      }
      else if( obj instanceof PropertiesFactoryBean) {
        PropertiesFactoryBean factory = (PropertiesFactoryBean) obj;
        System.out.println(factory);
      }
    }

    return obj;
  }

}
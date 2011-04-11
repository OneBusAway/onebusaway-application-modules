package org.onebusaway.container.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;

public class PropertyOverrideBeanFactoryPostProcessor extends PropertyOverrideConfigurer {

  private static Logger _log = LoggerFactory.getLogger(PropertyOverrideBeanFactoryPostProcessor.class);

  @Override
  protected void applyPropertyValue(ConfigurableListableBeanFactory factory,
      String beanName, String property, String value) {

    BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);

    if (beanDefinition == null) {
      _log.warn("could not find bean definition for bean named " + beanName);
      return;
    }

    RuntimeBeanReference ref = new RuntimeBeanReference(value);

    MutablePropertyValues pvs = beanDefinition.getPropertyValues();
    PropertyValue pv = pvs.getPropertyValue(property);

    if (pv == null) {
      pvs.addPropertyValue(property, ref);
    } else {
      pv.setConvertedValue(ref);
    }
  }
}

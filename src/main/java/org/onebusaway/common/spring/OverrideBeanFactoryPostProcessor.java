package org.onebusaway.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class OverrideBeanFactoryPostProcessor implements
    BeanFactoryPostProcessor {

  public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
      throws BeansException {

  }
}

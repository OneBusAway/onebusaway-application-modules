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

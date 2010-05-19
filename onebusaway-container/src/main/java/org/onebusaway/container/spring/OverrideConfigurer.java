package org.onebusaway.container.spring;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

public class OverrideConfigurer implements BeanFactoryPostProcessor,
    PriorityOrdered {

  private static Logger _log = LoggerFactory.getLogger(OverrideConfigurer.class);

  public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";

  private String _beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

  private boolean _ignoreInvalidKeys = false;

  private int _order = Ordered.LOWEST_PRECEDENCE;

  private Map<String, Object> _map = new HashMap<String, Object>();

  public void setMap(Map<String, Object> map) {
    _map = map;
  }

  public void setBeanNameSeparator(String beanNameSeparator) {
    this._beanNameSeparator = beanNameSeparator;
  }

  public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
    this._ignoreInvalidKeys = ignoreInvalidKeys;
  }

  public void setOrder(int order) {
    this._order = order;
  }

  @Override
  public int getOrder() {
    return _order;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    processMap(beanFactory,_map);
  }

  /****
   * Private Methods
   ****/

  protected void processMap(ConfigurableListableBeanFactory beanFactory,
      Map<String, Object> map) throws BeansException {

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      try {
        processKey(beanFactory, key, value);
      } catch (BeansException ex) {
        String msg = "Could not process key '" + key
            + "' in PropertyOverrideConfigurer";
        if (!_ignoreInvalidKeys) {
          throw new BeanInitializationException(msg, ex);
        }
        if (_log.isDebugEnabled())
          _log.debug(msg, ex);
      }
    }
  }

  protected void processKey(ConfigurableListableBeanFactory factory,
      String key, Object value) throws BeansException {

    int separatorIndex = key.indexOf(_beanNameSeparator);
    if (separatorIndex == -1) {
      throw new BeanInitializationException("Invalid key '" + key
          + "': expected 'beanName" + this._beanNameSeparator + "property'");
    }
    String beanName = key.substring(0, separatorIndex);
    String beanProperty = key.substring(separatorIndex + 1);
    applyPropertyValue(factory, beanName, beanProperty, value);
  }

  protected void applyPropertyValue(ConfigurableListableBeanFactory factory,
      String beanName, String property, Object value) {

    BeanDefinition bd = factory.getBeanDefinition(beanName);
    while (bd.getOriginatingBeanDefinition() != null) {
      bd = bd.getOriginatingBeanDefinition();
    }
    bd.getPropertyValues().addPropertyValue(property, value);
  }

}

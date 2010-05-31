package org.onebusaway.container.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * Similar in spirit to Spring's {@link PropertyOverrideConfigurer}, our
 * {@link OverrideConfigurer} accepts a {@code map} property that a {@link Map}
 * of String keys of the form "beanName.propertyName" and values that are
 * arbitrary Java objects, including references to other Spring beans. This is
 * more powerful than {@link PropertyOverrideConfigurer}, which only allows
 * String values.
 * 
 * Because this is a {@link BeanPostProcessor}, property expressions in keys or
 * values (ex. {@code "$ some.java.property}"}) will not be expanded if you are
 * using a {@link PropertyPlaceholderConfigurer}. We do some basic property
 * expansion on our own to alleviate this behavior.
 * 
 * @author bdferris
 */
public class OverrideConfigurer implements BeanFactoryPostProcessor,
    PriorityOrdered {

  private static final Pattern _propertySubstitution = Pattern.compile("\\$\\{(.*?)\\}");

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
    processMap(beanFactory, _map);
  }

  /****
   * Private Methods
   ****/

  protected void processMap(ConfigurableListableBeanFactory beanFactory,
      Map<String, Object> map) throws BeansException {

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = processValue(entry.getValue());

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

  protected Object processValue(Object value) {
    if (value instanceof String) {
      String v = (String) value;

      Matcher m = _propertySubstitution.matcher(v);
      StringBuffer sb = new StringBuffer();
      while (m.find()) {
        String propName = m.group(1);
        String propValue = System.getProperty(propName);
        if (propValue != null)
          m.appendReplacement(sb, propValue);
        else
          m.appendReplacement(sb, "${" + propName + "}");
      }
      m.appendTail(sb);
      value = sb.toString();
    }
    return value;
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

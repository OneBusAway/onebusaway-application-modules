package org.onebusaway.container.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * A Spring {@link FactoryBean} for instantiating a {@link Proxy} instance with
 * a specified target interface type and a {@link InvocationHandler}.
 * 
 * @author bdferris
 * 
 */
public class ProxyFactoryBean extends AbstractFactoryBean {

  private Class<?> _proxyInterface;

  private InvocationHandler _invocationHandler;

  public void setProxyInterface(Class<?> proxyInterface) {
    _proxyInterface = proxyInterface;
  }

  public void setInvocationHandler(InvocationHandler invocationHandler) {
    _invocationHandler = invocationHandler;
  }

  @Override
  public Class<?> getObjectType() {
    return _proxyInterface;
  }

  @Override
  protected Object createInstance() throws Exception {
    return Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class<?>[] {_proxyInterface}, _invocationHandler);
  }

}

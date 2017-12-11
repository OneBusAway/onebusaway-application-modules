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
package org.onebusaway.enterprise.webapp.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Properties;

public class TypedMessagesFactory extends AbstractFactoryBean {

  private final Logger _log = LoggerFactory.getLogger(TypedMessagesFactory.class);

  private Class<?> _messagesClass;

  public void setMessagesClass(Class<?> messagesClass) {
    _messagesClass = messagesClass;
  }

  @Override
  public Class<?> getObjectType() {
    return _messagesClass;
  }

  protected Object createInstance() throws IOException {
    String name = _messagesClass.getName();
    name = "/" + name.replace('.', '/') + ".properties";
    InputStream is = _messagesClass.getResourceAsStream(name);
    if (is == null)
      throw new IllegalStateException("Unable to find resources: " + name);
    Properties p = new Properties();
    p.load(is);

    for (Method method : _messagesClass.getDeclaredMethods()) {
      if (!p.containsKey(method.getName()))
        _log.warn("missing message name: messagesType="
            + _messagesClass.getName() + " property=" + method.getName());
    }

    Handler handler = new Handler(p);
    return Proxy.newProxyInstance(_messagesClass.getClassLoader(),
        new Class[] {_messagesClass}, handler);
  }

  private static class Handler implements InvocationHandler {

    private Properties _properties;

    public Handler(Properties properties) {
      _properties = properties;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

      String key = method.getName();
      if (!_properties.containsKey(key))
        throw new IllegalArgumentException("no such message: " + key);
      String value = _properties.getProperty(key);
      MessageFormat format = new MessageFormat(value);
      StringBuffer b = new StringBuffer();
      format.format(args, b, null);
      return b.toString();
    }
  }
}

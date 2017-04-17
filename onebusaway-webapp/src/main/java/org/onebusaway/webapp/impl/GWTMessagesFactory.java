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
package org.onebusaway.webapp.impl;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.google.gwt.i18n.client.Messages;
import com.opensymphony.xwork2.ActionContext;

public class GWTMessagesFactory extends GWTAbstractFactory {

  public GWTMessagesFactory() {
    super(Messages.class);
  }

  protected Object createInstance() throws IOException {
    String name = _type.getName();
    name = "/" + name.replace('.', '/');  
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename(name);
    Handler handler = new Handler(messageSource);
    return Proxy.newProxyInstance(_type.getClassLoader(), new Class[] {_type},
        handler);
  }

  private static class Handler implements InvocationHandler {

    private MessageSource _messageSource;

    public Handler(MessageSource messageSource) {
      _messageSource = messageSource;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

    	Locale local = null;
    	try {
    		ActionContext ctx = ActionContext.getContext();
    		if (ctx != null)
    			local  = ctx.getLocale();
    		} catch(Throwable e) {
    		}
    	
    	return _messageSource.getMessage(method.getName(), args,local);
    }
  }
}

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
package org.onebusaway.presentation.impl.struts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.DefaultActionProxyFactory;
import com.opensymphony.xwork2.inject.Inject;

public class MultiActionProxyFactory extends DefaultActionProxyFactory {

  public static final String MAPPINGS = "org.onebusaway.presentation.impl.struts.MultiActionProxyFactory.mapping";

  private List<Prefixed<ActionProxyFactory>> _prefixedActionProxyFactories = new ArrayList<Prefixed<ActionProxyFactory>>();

  @Inject(MAPPINGS)
  public void setActionMappers(String list) {
    if (list != null) {
      String[] tokens = list.split(",");
      for (String token : tokens) {
        String[] kvp = token.split("=");
        String key = kvp[0];
        String name = kvp[1];
        ActionProxyFactory factory = container.getInstance(
            ActionProxyFactory.class, name);
        if (factory != null) {
          _prefixedActionProxyFactories.add(new Prefixed<ActionProxyFactory>(
              key, factory));
        } else {
          throw new IllegalStateException("unknown ActionProxyFactory " + name);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActionProxy createActionProxy(String namespace, String actionName,
      String methodName, @SuppressWarnings("rawtypes") Map extraContext,
      boolean executeResult, boolean cleanupContext) {

    for (Prefixed<ActionProxyFactory> prefixedActionMapper : _prefixedActionProxyFactories) {

      String prefix = prefixedActionMapper.getPrefix();
      if (namespace.startsWith(prefix)) {
        ActionProxyFactory actionProxyFactory = prefixedActionMapper.getValue();
        return actionProxyFactory.createActionProxy(namespace, actionName,
            methodName, extraContext, executeResult, cleanupContext);
      }
    }

    throw new IllegalArgumentException("no ActionProxyFactory for namespace: "
        + namespace);
  }

  @Override
  public ActionProxy createActionProxy(ActionInvocation inv, String namespace,
      String actionName, String methodName, boolean executeResult,
      boolean cleanupContext) {

    for (Prefixed<ActionProxyFactory> prefixedActionMapper : _prefixedActionProxyFactories) {

      String prefix = prefixedActionMapper.getPrefix();
      if (namespace.startsWith(prefix)) {
        ActionProxyFactory actionProxyFactory = prefixedActionMapper.getValue();
        return actionProxyFactory.createActionProxy(inv, namespace, actionName,
            methodName, executeResult, cleanupContext);
      }
    }

    throw new IllegalArgumentException("no ActionProxyFactory for namespace: "
        + namespace);
  }

}

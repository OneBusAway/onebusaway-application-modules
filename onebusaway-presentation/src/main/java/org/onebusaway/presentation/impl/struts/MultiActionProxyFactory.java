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

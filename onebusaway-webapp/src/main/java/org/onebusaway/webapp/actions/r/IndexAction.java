package org.onebusaway.webapp.actions.r;

import org.apache.struts2.convention.annotation.Namespace;
import org.onebusaway.presentation.impl.resources.ResourceAction;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

@Namespace("/r/**")
public class IndexAction extends ResourceAction {

  private static final long serialVersionUID = 1L;

  @Override
  protected void ensureResource() {
    if (_id == null) {
      ActionContext context = ActionContext.getContext();
      ActionInvocation invocation = context.getActionInvocation();
      ActionProxy proxy = invocation.getProxy();

      String ns = proxy.getNamespace();
      String actionName = proxy.getActionName();
      
      if( ns.startsWith("/r"))
        ns = ns.substring("/r".length());
      
      _id = ns + "/" + actionName;
    }

    super.ensureResource();
  }
}

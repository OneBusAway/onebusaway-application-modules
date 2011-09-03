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

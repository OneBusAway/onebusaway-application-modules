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
package org.onebusaway.presentation.impl.users;

import java.util.UUID;

import org.springframework.web.context.request.RequestContextHolder;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class RequestContextInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private static final String KEY_SESSION_ID = RequestContextInterceptor.class.getName()
      + ".sessionId";

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    ActionContext context = invocation.getInvocationContext();
    String id = getSessionId(context);

    XWorkRequestAttributes attributes = new XWorkRequestAttributes(context, id);
    RequestContextHolder.setRequestAttributes(attributes);

    String result = invocation.invoke();

    RequestContextHolder.resetRequestAttributes();
    attributes.requestCompleted();

    return result;
  }

  private String getSessionId(ActionContext context) {
    String id = (String) context.getSession().get(KEY_SESSION_ID);
    if (id == null) {
      UUID uuid = UUID.randomUUID();
      id = uuid.toString();
      context.getSession().put(KEY_SESSION_ID, id);
    }
    return id;
  }
}

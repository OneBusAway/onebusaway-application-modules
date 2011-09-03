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
package org.onebusaway.phone.impl;

import java.util.Map;
import java.util.UUID;

import org.onebusaway.presentation.impl.users.XWorkRequestAttributes;
import org.onebusaway.probablecalls.AgiEntryPoint;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.opensymphony.xwork2.ActionContext;

/**
 * This custom AgiEntryPoint class exists soley to setup the
 * RequestContextHolder before the action factory is invoked in the
 * AgiEntryPoint, and then clean up afterwards
 * 
 * @author bdferris
 * 
 */
public class CustomAgiEntryPoint extends AgiEntryPoint {

  @Override
  protected void onActionSetup(Map<String, Object> contextMap) {

    super.onActionSetup(contextMap);

    String sessionId = UUID.randomUUID().toString();
    XWorkRequestAttributes attributes = new XWorkRequestAttributes(
        new ActionContext(contextMap), sessionId);
    RequestContextHolder.setRequestAttributes(attributes);
  }

  @Override
  protected void onActionTearDown() {

    super.onActionTearDown();

    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    RequestContextHolder.resetRequestAttributes();
    if (attributes instanceof XWorkRequestAttributes)
      ((XWorkRequestAttributes) attributes).requestCompleted();
  }

}

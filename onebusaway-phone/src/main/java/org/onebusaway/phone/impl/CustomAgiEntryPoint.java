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

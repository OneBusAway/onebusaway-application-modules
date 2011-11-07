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
package org.onebusaway.sms.actions.sms;

import java.util.Map;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.logging.UserInteractionLoggingService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

@InterceptorRefs( {@InterceptorRef("onebusaway-sms-webapp-stack")})
@Results( {
    @Result(type = "chain", name = "stop-by-number", location = "stop-by-number"),
    @Result(type = "chain", name = "arrivals-and-departures", location = "arrivals-and-departures"),
    @Result(type = "chain", name = "command", location = "command"),
    @Result(type = "chain", name = "handle-multi-selection", location = "handle-multi-selection"),
    @Result(type = "chain", name = "query-default-search-location", location = "query-default-search-location"),
    @Result(type = "chain", name = "set-default-search-location", location = "set-default-search-location"),
    @Result(type = "chain", name = "cancel", location = "cancel")})
public class AbstractTextmarksAction extends NextActionSupport {

  private static final long serialVersionUID = 1L;

  protected TransitDataService _transitDataService;

  protected CurrentUserService _currentUserService;

  protected String _text;

  private UserInteractionLoggingService _userInteractionLoggingService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @Autowired
  public void setUserInteractionLoggingService(
      UserInteractionLoggingService userInteractionLoggingService) {
    _userInteractionLoggingService = userInteractionLoggingService;
  }
  
  public void setMessage(String message) {
    if (_text == null)
      _text = message.trim();
  }

  public void setText(String text) {
    _text = text;
  }

  public String getText() {
    return _text;
  }
  
  protected void logUserInteraction(Object... objects) {

    Map<String, Object> entry = _userInteractionLoggingService.isInteractionLoggedForCurrentUser();

    if (entry == null)
      return;

    ActionContext context = ActionContext.getContext();
    ActionInvocation invocation = context.getActionInvocation();
    ActionProxy proxy = invocation.getProxy();

    entry.put("interface", "sms");
    entry.put("namespace", proxy.getNamespace());
    entry.put("actionName", proxy.getActionName());
    entry.put("method", proxy.getMethod());
    
    if( objects.length % 2 != 0 )
      throw new IllegalStateException("expected an even number of arguments");
      
    for( int i=0; i<objects.length; i+= 2)
      entry.put(objects[i].toString(),objects[i+1]);

    _userInteractionLoggingService.logInteraction(entry);
  }
}

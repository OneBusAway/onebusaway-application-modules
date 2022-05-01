/**
 * Copyright (C) 2014 HART (Hillsborough Area Regional Transit) 
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
package org.onebusaway.twilio.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.opensymphony.xwork2.util.StrutsLocalizedTextProvider;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.twilio.actions.stops.StopForCodeAction;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.util.ValueStack;

public class TwilioSupport extends ActionSupport implements ParameterAware, CurrentUserAware, SessionAware {

  public static final String PREVIOUS_MENU_ITEM = "*";
  public static final String REPEAT_MENU_ITEM = "8";
  public static final String INPUT_KEY = "Digits";
  public static final String PHONE_NUMBER_KEY = "From";
  public static final String NEEDS_DEFAULT_SEARCH_LOCATION = "needsDefaultSearchLocation";
  protected static final int DISPLAY_DATA = 0;
  protected static final int DO_ROUTING = 1;
  
  private static Logger _log = LoggerFactory.getLogger(StopForCodeAction.class);
  
  protected TransitDataService _transitDataService;
  protected CurrentUserService _currentUserService;
  private ServiceAreaService _serviceAreaService;
  protected Map<String, String[]> _parameters;
  private StringBuffer _message = new StringBuffer();
  protected UserBean _currentUser;
  protected Map sessionMap;
  
  protected void addText(String txt) {
    _log.debug(txt);
    _message.append(txt);
  }
  
  protected void addMessage(String msg) {
    _log.debug(msg);
    _message.append(" " + getText(msg) + " ");
  }
  
  protected void addMessage(String msg, Object... args) {
    ActionContext context = ActionContext.getContext();
    Locale locale = context.getLocale();
    ValueStack valueStack = context.getValueStack();
    String text = new StrutsLocalizedTextProvider().findText(TwilioSupport.this.getClass(), msg, locale, msg, args, valueStack);
    _log.debug("message: " + text);
    _message.append(" " + text + " ");
    _log.debug(getText(msg));
  }
  
  public String getMessage() {
    return _message.toString();
  }
  
  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }
  
  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }
  @Override
  public void setParameters(Map<String, String[]> arg0) {
    _parameters = arg0;
  }
  
  @Override
  public void setSession(Map map) {
    this.sessionMap = map;
  }
  
  public String getInput() {
    if (_parameters != null && _parameters.containsKey(INPUT_KEY)) {
      Object val = _parameters.get(INPUT_KEY);
      if (val instanceof String[]) {
        return ((String[])val)[0];
      }
      return (String)val;
    }
    return null;
  }
  
  public String clearInput() {
    if (_parameters != null && _parameters.containsKey(INPUT_KEY)) {
      _parameters.remove(INPUT_KEY);
      Object val = _parameters.remove(INPUT_KEY);
      if (val instanceof String[]) {
        return ((String[])val)[0];
      }
      return (String)val;
    }
    return null;
  }
  
  public String getPhoneNumber() {
    if (_parameters != null && _parameters.containsKey(PHONE_NUMBER_KEY)) {
    	Object val = _parameters.get(PHONE_NUMBER_KEY);
    	if (val instanceof String[]) {
      	return ((String[])val)[0];
    	}
      return (String)val;
    }
    return null;
  }

  protected void setNextAction(String actionName) {
    _log.debug("next action now " + actionName);
    ActionContext.getContext().getSession().put("twilio.nextAction", actionName);
  }
  
  protected void clearNextAction() {
    _log.debug("next action cleared");
    ActionContext.getContext().getSession().remove("twilio.nextAction");
  }
  
  protected CoordinateBounds getDefaultSearchArea() {
    return _serviceAreaService.getServiceArea();
  }
  
  protected void logUserInteraction(Object... objects) {
	  String text = "logUserInteraction(";
	  for (int i=0; i<objects.length; ++i) {
		  text += objects[i].toString();
		  if (i<objects.length-1) {
			  text += ", ";
		  }
	  }
	  text += ")";
	  _log.info(text);
  }
  
  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  @Override
  public void setCurrentUser(UserBean currentUser) {
    _currentUser = currentUser;
  }

  public UserBean getCurrentUser() {
    return _currentUser;
  }

  protected void clearNavState() {
    sessionMap.remove("navState");
  }

}

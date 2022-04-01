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
package org.onebusaway.twilio.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.twilio.services.SessionManager;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FOR TESTING ONLY.  This is not used in the production workflow.  Instead
 * Twilio follows the redirects.
 */
public class TwilioDispatchFilter implements Filter {

  private static final long serialVersionUID = 1L;
  
  private static Logger _log = LoggerFactory.getLogger(TwilioDispatchFilter.class);
  
  private SessionManager _sessionManager = null;
  private static final String PHONE_NUMBER_KEY = "From";
  public static final String NEXT_ACTION = "twilio.nextAction";
  private static final String INDEX_ACTION = "index";
  private static final String WELCOME_ACTION = "welcome";

  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
    String key = request.getParameter(PHONE_NUMBER_KEY);
    
    char c = key.charAt(0);
    if (!Character.isDigit(c)) {
    	String tempKey = "";
    	for (int i=0; i<key.length(); ++i) {
    		c = key.charAt(0);
    		if (!Character.isDigit(c)) {
    			if (tempKey.length() > 0) break;
    			continue;
    		}
    		tempKey += c;
    	}
    	key = tempKey;
    }
    _log.debug("key: " + key);
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    // if we've seen this user before
    if (_sessionManager.hasContext(key)) {
      _log.debug("found session=" + _sessionManager.getContext(key));
      // redirect to last known location
      Map<String, Object> context = _sessionManager.getContext(key);
      String nextAction = (String) context.get(NEXT_ACTION);
      if (nextAction != null) {
        _log.debug("forwarding to " + nextAction);
        httpResponse.sendRedirect(format(httpRequest, nextAction));
        return;
      }
    }

    _log.debug("fall through forwarding to " + WELCOME_ACTION);
    // redirect to welcome page
    httpResponse.sendRedirect(format(httpRequest, WELCOME_ACTION));
  }

  private String format(HttpServletRequest request, String nextAction) {
    
    String path = nextAction + "?_foo=" + SystemTime.currentTimeMillis();

    // copy the path to the forwarded request
    for (Object key : request.getParameterMap().keySet()) {
      path = path + "&" + key + "=" + request.getParameter((String)key);
    }
    return path;
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    _sessionManager = (SessionManager) config.getServletContext().getAttribute("twilioSessionManager");
  }

  @Override
  public void destroy() {
    // nothing to do
  }
   

}

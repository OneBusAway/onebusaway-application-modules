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
package org.onebusaway.twilio.actions.stops;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionContext;

@Results({
  @Result(name="back", type="redirectAction", params={"From", "${phoneNumber}", "namespace", "/", "actionName", "index"}),
  @Result(name="success", location="arrivals-and-departures-for-stop-id", type="chain")
})
public class MostRecentStopAction extends TwilioSupport implements SessionAware {

  private static final long serialVersionUID = 1L;
	private static Logger _log = LoggerFactory.getLogger(MostRecentStopAction.class);
	private Map sessionMap;

  private List<String> _stopIds;

  public List<String> getStopIds() {
    return _stopIds;
  }

	public void setSession(Map map) {
	  this.sessionMap = map;
	}
		
  @Override
  public String execute() throws Exception {
    Integer navState = (Integer)sessionMap.get("navState");
    _log.debug("execute - navState: " + navState);
    
    if (navState == DO_ROUTING) {
      sessionMap.put("navState", DISPLAY_DATA);
      return "back";
    }
    
    _stopIds = _currentUser.getLastSelectedStopIds();

    if (_stopIds == null || _stopIds.isEmpty()) {
      _log.debug("stopIds is null or empty");
      sessionMap.put("navState", DO_ROUTING);
      return INPUT;
    }
    
    logUserInteraction("stopIds", _stopIds);

    return SUCCESS;
  }
}

/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Results({
        @Result(name = "welcome", location = "welcome", type = "redirectAction", params = {"From", "${phoneNumber}"})
})
/**
 * Front Door Action to Twilio.  Do any session cleanup from past runs if session is
 * active.
 */
public class TwilioEntryAction extends TwilioSupport implements SessionAware {

  public void setSession(Map map) {
    this.sessionMap = map;
  }

  @Override
  public String execute() {
    logUserInteraction();


    if (this.sessionMap != null && !this.sessionMap.isEmpty()) {

      Set set = new HashSet(sessionMap.keySet());
      // clean up session for next use
      for (Object o : set) {
        if (o instanceof String) {
          String s = (String) o;
          sessionMap.remove(s);
        }
      }
    }
    return "welcome";
  }
}
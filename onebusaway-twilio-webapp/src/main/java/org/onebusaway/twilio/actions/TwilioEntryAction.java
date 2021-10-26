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
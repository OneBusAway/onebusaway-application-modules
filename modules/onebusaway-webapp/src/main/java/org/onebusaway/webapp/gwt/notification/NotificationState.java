package org.onebusaway.webapp.gwt.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationState {

  private int minutesBefore;

  private List<NotificationMethodState> methodStates = new ArrayList<NotificationMethodState>();

  public int getMinutesBefore() {
    return minutesBefore;
  }

  public void setMinutesBefore(int minutesBefore) {
    this.minutesBefore = minutesBefore;
  }

  public List<NotificationMethodState> getMethodStates() {
    return methodStates;
  }

  public void setMethodStates(List<NotificationMethodState> methodStates) {
    this.methodStates = methodStates;
  }
}

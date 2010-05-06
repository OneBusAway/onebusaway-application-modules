package org.onebusaway.webapp.gwt.notification;

public class NotificationStateNode implements Comparable<NotificationStateNode> {

  private String stopId;

  private int index;
  
  private NotificationState state;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int compareTo(NotificationStateNode o) {
    return this.index - o.index;
  }

  public NotificationState getState() {
    return state;
  }

  public void setState(NotificationState state) {
    this.state = state;
  }
}
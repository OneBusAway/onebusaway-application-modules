package org.onebusaway.api.model.transit;

import java.io.Serializable;

public class StopCalendarDayV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long date;

  private int group;

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

}

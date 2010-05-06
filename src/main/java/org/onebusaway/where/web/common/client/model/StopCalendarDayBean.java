package org.onebusaway.where.web.common.client.model;

import org.onebusaway.common.web.common.client.model.ApplicationBean;

import java.util.Date;

public class StopCalendarDayBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private Date date;

  private int group;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

}

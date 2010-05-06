package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class TimeBean implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private static final SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  private long time;
  
  // this conforms to ISO 8601
  private String readableTime;
  
  public TimeBean(Date date) {
	  setDate(date);
  }
  
  public void setDate(Date date) {
	  time = date.getTime();
    String timeString = _format.format(date);
	  readableTime = timeString.substring(0, timeString.length()-2) + ":" + timeString.substring(timeString.length()-2);
  }

  public long getTime() {
    return time;
  }

  public String getReadableTime() {
    return readableTime;
  }
}

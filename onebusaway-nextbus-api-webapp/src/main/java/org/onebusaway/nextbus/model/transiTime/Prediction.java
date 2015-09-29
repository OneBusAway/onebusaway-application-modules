package org.onebusaway.nextbus.model.transiTime;

import org.onebusaway.nextbus.impl.xstream.FalseConverter;
import org.onebusaway.nextbus.impl.xstream.RemoveEmptyConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("prediction")
public class Prediction {
  
  @XStreamAsAttribute
  @XStreamAlias("seconds")
  private int sec;
  
  @XStreamAsAttribute
  @XStreamAlias("minutes")
  private int min;
  
  @XStreamAsAttribute
  @XStreamAlias("epochTime")
  private long time;
  
  @XStreamAsAttribute
  @XStreamAlias("isDeparture")
  private boolean notYetDeparted;
  
  @XStreamAsAttribute
  @XStreamAlias("isDeparture")
  private String dirTag;
  
  @XStreamAsAttribute
  @XStreamAlias("block")
  private String vehicle;
  
  @XStreamAsAttribute
  @XStreamAlias("tripTag")
  @XStreamConverter(RemoveEmptyConverter.class)
  private String trip;
  
  @XStreamAsAttribute
  @XStreamAlias("isScheduleBased")
  @XStreamConverter(FalseConverter.class)
  private Boolean scheduleBased;
  
  @XStreamAsAttribute
  @XStreamAlias("affectedByLayover")
  @XStreamConverter(FalseConverter.class)
  private boolean affectedByLayover;
  
  @XStreamAsAttribute
  @XStreamAlias("delayed")
  @XStreamConverter(FalseConverter.class)
  private boolean delayed;
  
  public Prediction(){}

  public int getSec() {
    return sec;
  }

  public void setSec(int sec) {
    this.sec = sec;
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public boolean isNotYetDeparted() {
    return notYetDeparted;
  }

  public void setNotYetDeparted(boolean notYetDeparted) {
    this.notYetDeparted = notYetDeparted;
  }

  public String getVehicle() {
    return vehicle;
  }

  public void setVehicle(String vehicle) {
    this.vehicle = vehicle;
  }

  public String getTrip() {
    return trip;
  }

  public void setTrip(String trip) {
    this.trip = trip;
  }

  public Boolean isScheduleBased() {
    return scheduleBased;
  }

  public void setScheduleBased(boolean scheduleBased) {
    if(scheduleBased)
      this.scheduleBased = scheduleBased;
  }

  public String getDirTag() {
    return dirTag;
  }

  public void setDirTag(String dirTag) {
    this.dirTag = dirTag;
  }

  public boolean getAffectedByLayover() {
    return affectedByLayover;
  }

  public void setAffectedByLayover(boolean affectedByLayover) {
    if(affectedByLayover)
      this.affectedByLayover = affectedByLayover;
  }
 
}


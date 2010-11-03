package org.onebusaway.transit_data.model;

import java.io.Serializable;

@QueryBean
public final class ArrivalsAndDeparturesQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time = System.currentTimeMillis();

  private int minutesBefore = 5;

  private int minutesAfter = 35;

  private int frequencyMinutesBefore = 2;

  private int frequencyMinutesAfter = 30;

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public int getMinutesBefore() {
    return minutesBefore;
  }

  public void setMinutesBefore(int minutesBefore) {
    this.minutesBefore = minutesBefore;
  }

  public int getMinutesAfter() {
    return minutesAfter;
  }

  public void setMinutesAfter(int minutesAfter) {
    this.minutesAfter = minutesAfter;
  }

  public int getFrequencyMinutesBefore() {
    return frequencyMinutesBefore;
  }

  public void setFrequencyMinutesBefore(int frequencyMinutesBefore) {
    this.frequencyMinutesBefore = frequencyMinutesBefore;
  }

  public int getFrequencyMinutesAfter() {
    return frequencyMinutesAfter;
  }

  public void setFrequencyMinutesAfter(int frequencyMinutesAfter) {
    this.frequencyMinutesAfter = frequencyMinutesAfter;
  }

}

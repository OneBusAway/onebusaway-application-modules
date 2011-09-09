package org.onebusaway.transit_data_federation.model;

import java.io.Serializable;

public class RunData implements Serializable {
  private static final long serialVersionUID = 1L;

  public String initialRun;
  public String reliefRun;
  public int reliefTime;
  
  public RunData(String run1, String run2, int reliefTime) {
    this.initialRun = run1;
    this.reliefRun = run2;
    this.reliefTime = reliefTime;
  }
}

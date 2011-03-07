package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;

public class TransitShedConstraintsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private ConstraintsBean constraints = new ConstraintsBean();

  private long maxInitialWaitTime;
  
  public TransitShedConstraintsBean() {
    
  }
  
  public TransitShedConstraintsBean(TransitShedConstraintsBean c) {
    this.maxInitialWaitTime = c.maxInitialWaitTime;
    this.constraints = new ConstraintsBean(c.getConstraints());
  }

  public ConstraintsBean getConstraints() {
    return constraints;
  }

  public void setConstraints(ConstraintsBean constraints) {
    this.constraints = constraints;
  }

  public long getMaxInitialWaitTime() {
    return maxInitialWaitTime;
  }

  public void setMaxInitialWaitTime(long maxInitialWaitTime) {
    this.maxInitialWaitTime = maxInitialWaitTime;
  }
}

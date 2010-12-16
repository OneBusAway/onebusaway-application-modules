package org.onebusaway.transit_data.model.problems;

import java.io.Serializable;

import org.onebusaway.transit_data.model.StopBean;

public class StopProblemReportSummaryBean implements
    Comparable<StopProblemReportSummaryBean>, Serializable {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private EProblemReportStatus status;

  private int count;

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public EProblemReportStatus getStatus() {
    return status;
  }

  public void setStatus(EProblemReportStatus status) {
    this.status = status;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  /****
   * {@link Comparable} Interface
   ****/

  @Override
  public int compareTo(StopProblemReportSummaryBean o) {
    return o.count - count;
  }
}

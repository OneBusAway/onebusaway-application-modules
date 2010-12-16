package org.onebusaway.transit_data.model.problems;

import java.io.Serializable;

import org.onebusaway.transit_data.model.trips.TripBean;

public class TripProblemReportSummaryBean implements
    Comparable<TripProblemReportSummaryBean>, Serializable {

  private static final long serialVersionUID = 1L;

  private TripBean trip;

  private EProblemReportStatus status;

  private int count;

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
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
  public int compareTo(TripProblemReportSummaryBean o) {
    return o.count - count;
  }
}

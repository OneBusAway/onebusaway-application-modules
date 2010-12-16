package org.onebusaway.transit_data.model.problems;

import java.io.Serializable;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public class StopProblemReportSummaryQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private EProblemReportStatus status;

  private long timeFrom;

  private long timeTo;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public EProblemReportStatus getStatus() {
    return status;
  }

  public void setStatus(EProblemReportStatus status) {
    this.status = status;
  }

  public long getTimeFrom() {
    return timeFrom;
  }

  public void setTimeFrom(long timeFrom) {
    this.timeFrom = timeFrom;
  }

  public long getTimeTo() {
    return timeTo;
  }

  public void setTimeTo(long timeTo) {
    this.timeTo = timeTo;
  }
}

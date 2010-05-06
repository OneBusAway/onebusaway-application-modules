package org.onebusaway.webapp.gwt.oba_application.model;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;

public class TimedLocalSearchResult {

  private static final long serialVersionUID = 1L;

  private LocalSearchResult result;

  private TimedPlaceBean bean;

  private String resultId;

  public TimedLocalSearchResult() {

  }

  public TimedLocalSearchResult(String resultId, LocalSearchResult result,
      TimedPlaceBean bean) {
    this.resultId = resultId;
    this.result = result;
    this.bean = bean;
  }

  public String getResultId() {
    return this.resultId;
  }

  public String getId() {
    return result.getId();
  }

  /**
   * @return trip time in seconds
   */
  public int getTime() {
    return bean.getTime();
  }

  public LocalSearchResult getLocalSearchResult() {
    return result;
  }

  public TimedPlaceBean getTimedPlace() {
    return bean;
  }
}

package org.onebusaway.oba.web.standard.client.model;

import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.TimedPlaceBean;

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

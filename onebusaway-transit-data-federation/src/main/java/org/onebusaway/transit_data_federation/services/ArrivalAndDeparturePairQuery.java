package org.onebusaway.transit_data_federation.services;

public class ArrivalAndDeparturePairQuery {

  private int resultCount = 1;

  private boolean applyRealTime = false;

  private int lookaheadTime = 0;

  private boolean includePrivateService = false;

  public int getResultCount() {
    return resultCount;
  }

  public void setResultCount(int resultCount) {
    this.resultCount = resultCount;
  }

  public boolean isApplyRealTime() {
    return applyRealTime;
  }

  public void setApplyRealTime(boolean applyRealTime) {
    this.applyRealTime = applyRealTime;
  }

  public int getLookaheadTime() {
    return lookaheadTime;
  }

  public void setLookaheadTime(int lookaheadTime) {
    this.lookaheadTime = lookaheadTime;
  }

  public boolean isIncludePrivateService() {
    return includePrivateService;
  }

  public void setIncludePrivateService(boolean includePrivateService) {
    this.includePrivateService = includePrivateService;
  }
}

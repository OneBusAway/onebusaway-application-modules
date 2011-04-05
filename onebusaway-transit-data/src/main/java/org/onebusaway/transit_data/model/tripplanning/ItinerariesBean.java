package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItinerariesBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private LocationBean from = null;

  private LocationBean to = null;

  private List<ItineraryBean> itineraries = new ArrayList<ItineraryBean>();
  
  private boolean computationTimeLimitReached = false;

  public LocationBean getFrom() {
    return from;
  }

  public void setFrom(LocationBean from) {
    this.from = from;
  }

  public LocationBean getTo() {
    return to;
  }

  public void setTo(LocationBean to) {
    this.to = to;
  }

  public List<ItineraryBean> getItineraries() {
    return itineraries;
  }

  public void setItineraries(List<ItineraryBean> itineraries) {
    this.itineraries = itineraries;
  }

  public boolean isComputationTimeLimitReached() {
    return computationTimeLimitReached;
  }

  public void setComputationTimeLimitReached(boolean computationTimeLimitReached) {
    this.computationTimeLimitReached = computationTimeLimitReached;
  }
}

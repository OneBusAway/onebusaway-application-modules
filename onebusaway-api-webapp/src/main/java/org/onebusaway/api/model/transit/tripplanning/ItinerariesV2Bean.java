package org.onebusaway.api.model.transit.tripplanning;

import java.util.ArrayList;
import java.util.List;

public class ItinerariesV2Bean {

  private LocationV2Bean from = null;

  private LocationV2Bean to = null;

  private List<ItineraryV2Bean> itineraries = new ArrayList<ItineraryV2Bean>();

  public LocationV2Bean getFrom() {
    return from;
  }

  public void setFrom(LocationV2Bean from) {
    this.from = from;
  }

  public LocationV2Bean getTo() {
    return to;
  }

  public void setTo(LocationV2Bean to) {
    this.to = to;
  }

  public List<ItineraryV2Bean> getItineraries() {
    return itineraries;
  }

  public void setItineraries(List<ItineraryV2Bean> itineraries) {
    this.itineraries = itineraries;
  }
}

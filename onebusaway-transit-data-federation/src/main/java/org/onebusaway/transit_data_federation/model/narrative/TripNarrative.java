package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

/**
 * Trip narrative information. Includes information about the trip short name,
 * trip headsign, route shortname override, direction, and shape id.
 * 
 * @author bdferris
 * @see Trip
 * @see NarrativeService
 */
public final class TripNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String tripShortName;

  private final String tripHeadsign;

  private final String routeShortName;

  private final String directionId;

  public static Builder builder() {
    return new Builder();
  }

  private TripNarrative(Builder builder) {
    this.tripShortName = builder.tripShortName;
    this.tripHeadsign = builder.tripHeadsign;
    this.routeShortName = builder.routeShortName;
    this.directionId = builder.directionId;
  }

  public String getTripShortName() {
    return tripShortName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public String getDirectionId() {
    return directionId;
  }

  public static class Builder {

    private String tripShortName;

    private String tripHeadsign;

    private String routeShortName;

    private String directionId;

    public TripNarrative create() {
      return new TripNarrative(this);
    }

    public Builder setTripShortName(String tripShortName) {
      this.tripShortName = tripShortName;
      return this;
    }

    public Builder setTripHeadsign(String tripHeadsign) {
      this.tripHeadsign = tripHeadsign;
      return this;
    }

    public Builder setRouteShortName(String routeShortName) {
      this.routeShortName = routeShortName;
      return this;
    }

    public Builder setDirectionId(String directionId) {
      this.directionId = directionId;
      return this;
    }
  }
}

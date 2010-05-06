package org.onebusaway.transit_data_federation.model.narrative;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

public final class TripNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId id;

  private final AgencyAndId routeId;

  private final AgencyAndId serviceId;

  private final String tripShortName;

  private final String tripHeadsign;

  private final String routeShortName;

  private final String directionId;

  private final String blockId;

  private final int blockSequenceId;

  private final AgencyAndId shapeId;
  
  public static Builder builder() {
    return new Builder();
  }
  
  private TripNarrative(Builder builder) {
    this.id = builder.id;
    this.routeId = builder.routeId;
    this.serviceId = builder.serviceId;
    this.tripShortName = builder.tripShortName;
    this.tripHeadsign = builder.tripHeadsign;
    this.routeShortName = builder.routeShortName;
    this.directionId = builder.directionId;
    this.blockId = builder.blockId;
    this.blockSequenceId = builder.blockSequenceId;
    this.shapeId = builder.shapeId;
    
  }

  public AgencyAndId getId() {
    return id;
  }

  public AgencyAndId getRouteId() {
    return routeId;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
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

  public String getBlockId() {
    return blockId;
  }

  public int getBlockSequenceId() {
    return blockSequenceId;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public static class Builder {

    private AgencyAndId id;

    private AgencyAndId routeId;

    private AgencyAndId serviceId;

    private String tripShortName;

    private String tripHeadsign;

    private String routeShortName;

    private String directionId;

    private String blockId;

    private int blockSequenceId = -1;

    private AgencyAndId shapeId;
    
    public TripNarrative create() {
      return new TripNarrative(this);
    }

    public Builder setId(AgencyAndId id) {
      this.id = id;
      return this;
    }

    public Builder setRouteId(AgencyAndId routeId) {
      this.routeId = routeId;
      return this;
    }

    public Builder setServiceId(AgencyAndId serviceId) {
      this.serviceId = serviceId;
      return this;
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

    public Builder setBlockId(String blockId) {
      this.blockId = blockId;
      return this;
    }

    public Builder setBlockSequenceId(int blockSequenceId) {
      this.blockSequenceId = blockSequenceId;
      return this;
    }

    public Builder setShapeId(AgencyAndId shapeId) {
      this.shapeId = shapeId;
      return this;
    }
  }
}

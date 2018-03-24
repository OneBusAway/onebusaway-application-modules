/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2015 University of South Florida
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.realtime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.realtime.api.EVehicleType;

/**
 * A block location record is a database-serializable record that captures the
 * real-time position and schedule deviation for a transit vehicle at a
 * particular point in time. The record includes trip instance data and vehicle
 * id where available.
 * 
 * This class is mean for internal use.
 * 
 * @author bdferris
 */
@Entity
@Table(name = "transit_data_block_location_records")
@org.hibernate.annotations.Table(appliesTo = "transit_data_block_location_records", indexes = {
    @Index(name = "vehicle_and_time", columnNames = {
        "vehicle_agencyId", "vehicle_id", "time"}),
    @Index(name = "vehicle_and_serviceDate", columnNames = {
        "vehicle_agencyId", "vehicle_id", "serviceDate"})})
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class BlockLocationRecord {

  @Id
  @GeneratedValue
  private final int id = 0;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "block_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "block_id"))})
  private final AgencyAndId blockId;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  private final AgencyAndId tripId;

  private final long serviceDate;

  private final long time;

  @Column(nullable = true)
  private final Double scheduleDeviation;

  @Column(nullable = true)
  private final Double distanceAlongBlock;

  @Column(nullable = true)
  private final Double distanceAlongTrip;

  @Column(nullable = true)
  private final Double locationLat;

  @Column(nullable = true)
  private final Double locationLon;

  @Column(nullable = true)
  private final Double orientation;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "timepoint_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "timepoint_id"))})
  private final AgencyAndId timepointId;

  private final long timepointScheduledTime;

  private final long timepointPredictedArrivalTime;
  
  private final long timepointPredictedDepartureTime;

  /**
   * Custom Hibernate mapping so that the vehicle phase enum gets mapped to a
   * string as opposed to an integer, allowing for safe expansion of the enum in
   * the future and more legibility in the raw SQL. Additionally, the phase
   * string can be a little shorter than the default length.
   */
  @Type(type = "org.onebusaway.container.hibernate.EnumUserType", parameters = {@Parameter(name = "enumClassName", value = "org.onebusaway.realtime.api.EVehiclePhase")})
  @Column(length = 50)
  private final EVehiclePhase phase;

  @Type(type = "org.onebusaway.container.hibernate.EnumUserType", parameters = {@Parameter(name = "enumClassName", value = "org.onebusaway.realtime.api.EVehicleType")})
  @Column(length = 10)
  private final EVehicleType vehicleType;


  private final String status;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "vehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "vehicle_id"))})
  private final AgencyAndId vehicleId;

  public static Builder builder() {
    return new Builder();
  }

  public BlockLocationRecord() {
    blockId = null;
    tripId = null;
    serviceDate = 0;
    time = 0;
    scheduleDeviation = null;
    distanceAlongBlock = null;
    distanceAlongTrip = null;
    locationLat = null;
    locationLon = null;
    orientation = null;
    timepointId = null;
    timepointScheduledTime = 0;
    timepointPredictedArrivalTime = -1;
    timepointPredictedDepartureTime = -1;
    phase = null;
    status = null;
    vehicleId = null;
    vehicleType = EVehicleType.BUS;
  }

  private BlockLocationRecord(Builder builder) {
    this.blockId = builder.blockId;
    this.tripId = builder.tripId;
    this.serviceDate = builder.serviceDate;
    this.time = builder.time;
    this.scheduleDeviation = builder.scheduleDeviation;
    this.distanceAlongBlock = builder.distanceAlongBlock;
    this.distanceAlongTrip = builder.distanceAlongTrip;
    this.locationLat = builder.locationLat;
    this.locationLon = builder.locationLon;
    this.orientation = builder.orientation;
    this.timepointId = builder.timepointId;
    this.timepointScheduledTime = builder.timepointScheduledTime;
    this.timepointPredictedArrivalTime = builder.timepointPredictedArrivalTime;
    this.timepointPredictedDepartureTime = builder.timepointPredictedDepartureTime;
    this.phase = builder.phase;
    this.status = builder.status;
    this.vehicleId = builder.vehicleId;
    this.vehicleType = builder.vehicleType;
  }

  /**
   * @return a generated numeric id for this record
   */
  public int getId() {
    return id;
  }

  /**
   * @return the block id of the transit trip
   */
  public AgencyAndId getBlockId() {
    return blockId;
  }

  /**
   * @return the trip id of the transit trip
   */
  public AgencyAndId getTripId() {
    return tripId;
  }

  /**
   * @return the service date for the trip instance (Unix-time)
   */
  public long getServiceDate() {
    return serviceDate;
  }

  /**
   * @return the time the record was recorded (Unix-time)
   */
  public long getTime() {
    return time;
  }

  public boolean isScheduleDeviationSet() {
    return scheduleDeviation != null;
  }

  /**
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public Double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public boolean isDistanceAlongBlockSet() {
    return distanceAlongBlock != null;
  }

  /**
   * @return the distance traveled along the block
   */
  public Double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public Double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public boolean isLocationSet() {
    return locationLat != null && locationLon != null;
  }

  public Double getLocationLat() {
    return locationLat;
  }

  public Double getLocationLon() {
    return locationLon;
  }

  public CoordinatePoint getLocation() {
    if (!isLocationSet())
      return null;
    return new CoordinatePoint(locationLat, locationLon);
  }

  public boolean isOrientationSet() {
    return orientation != null;
  }

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  public Double getOrientation() {
    return orientation;
  }

  public AgencyAndId getTimepointId() {
    return timepointId;
  }

  public long getTimepointScheduledTime() {
    return timepointScheduledTime;
  }

  public long getTimepointPredictedArrivalTime() {
    return timepointPredictedArrivalTime;
  }

  public long getTimepointPredictedDepartureTime() {
    return timepointPredictedDepartureTime;
  }

  public EVehiclePhase getPhase() {
    return phase;
  }

  public EVehicleType getVehicleType() { return vehicleType; }

  public String getStatus() {
    return status;
  }

  /**
   * @return the vehicle id of the transit vehicle servicing the trip, when
   *         available
   */
  @Index(name = "vehicleId")
  public AgencyAndId getVehicleId() {
    return this.vehicleId;
  }

  @Override
  public String toString() {
    return "BlockLocationRecord(id=" + id + " blockId=" + blockId
        + " serviceDate=" + serviceDate + " time=" + time
        + " scheduleDeviation=" + scheduleDeviation + " vehicleId=" + vehicleId
        + ")";
  }

  public static class Builder {

    private AgencyAndId blockId;

    private AgencyAndId tripId;

    private long serviceDate;

    private long time;

    private Double scheduleDeviation = null;

    private Double distanceAlongBlock = null;

    private Double distanceAlongTrip = null;

    private Double locationLat = null;

    private Double locationLon = null;

    private Double orientation = null;

    private AgencyAndId timepointId;

    private long timepointScheduledTime;

    private long timepointPredictedArrivalTime;

    private long timepointPredictedDepartureTime;

    private EVehiclePhase phase;

    private EVehicleType vehicleType;

    private String status;

    private AgencyAndId vehicleId;

    public void setBlockId(AgencyAndId blockId) {
      this.blockId = blockId;
    }

    public void setTripId(AgencyAndId tripId) {
      this.tripId = tripId;
    }

    public void setServiceDate(long serviceDate) {
      this.serviceDate = serviceDate;
    }

    public void setTime(long time) {
      this.time = time;
    }

    public void setScheduleDeviation(Double scheduleDeviation) {
      this.scheduleDeviation = scheduleDeviation;
    }

    public void setDistanceAlongBlock(Double distanceAlongBlock) {
      this.distanceAlongBlock = distanceAlongBlock;
    }

    public void setDistanceAlongTrip(Double distanceAlongTrip) {
      this.distanceAlongTrip = distanceAlongTrip;
    }

    public void setLocationLat(Double locationLat) {
      this.locationLat = locationLat;
    }

    public void setLocationLon(Double locationLon) {
      this.locationLon = locationLon;
    }

    public void setLocation(CoordinatePoint location) {
      if (location == null) {
        this.locationLat = null;
        this.locationLon = null;
      } else {
        this.locationLat = location.getLat();
        this.locationLon = location.getLon();
      }
    }

    public void setOrientation(Double orientation) {
      this.orientation = orientation;
    }

    public void setTimepointId(AgencyAndId timepointId) {
      this.timepointId = timepointId;
    }

    public void setTimepointScheduledTime(long timepointScheduledTime) {
      this.timepointScheduledTime = timepointScheduledTime;
    }

    public void setTimepointPredictedArrivalTime(long timepointPredictedArrivalTime) {
      this.timepointPredictedArrivalTime = timepointPredictedArrivalTime;
    }

    public void setTimepointPredictedDepartureTime(long timepointPredictedDepartureTime) {
      this.timepointPredictedDepartureTime = timepointPredictedDepartureTime;
    }

    public void setPhase(EVehiclePhase phase) {
      this.phase = phase;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public EVehicleType getVehicleType() { return vehicleType; }

    public void setVehicleType(EVehicleType vehicleType) { this.vehicleType = vehicleType; }

    public void setVehicleId(AgencyAndId vehicleId) {
      this.vehicleId = vehicleId;
    }

    public BlockLocationRecord create() {
      return new BlockLocationRecord(this);
    }

  }
}

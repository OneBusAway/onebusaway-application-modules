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
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;

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
@org.hibernate.annotations.Table(appliesTo = "transit_data_block_location_records", indexes = {@Index(name = "vehicle_and_time", columnNames = {
    "vehicle_agencyId", "vehicle_id", "time"})})
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

  private final double scheduleDeviation;

  private final double distanceAlongBlock;

  private final double locationLat;

  private final double locationLon;

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
    scheduleDeviation = Double.NaN;
    distanceAlongBlock = Double.NaN;
    locationLat = Double.NaN;
    locationLon = Double.NaN;
    vehicleId = null;
  }

  private BlockLocationRecord(Builder builder) {
    this.blockId = builder.blockId;
    this.tripId = builder.tripId;
    this.serviceDate = builder.serviceDate;
    this.time = builder.time;
    this.scheduleDeviation = builder.scheduleDeviation;
    this.distanceAlongBlock = builder.distanceAlongBlock;
    this.locationLat = builder.locationLat;
    this.locationLon = builder.locationLon;
    this.vehicleId = builder.vehicleId;
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

  public boolean hasScheduleDeviation() {
    return !Double.isNaN(scheduleDeviation);
  }

  /**
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public boolean hasDistanceAlongBlock() {
    return !Double.isNaN(distanceAlongBlock);
  }

  /**
   * @return the distance traveled along the block
   */
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public boolean hasLocation() {
    return !(Double.isNaN(locationLat) || Double.isNaN(locationLon));
  }

  public double getLocationLat() {
    return locationLat;
  }

  public double getLocationLon() {
    return locationLon;
  }

  public CoordinatePoint getLocation() {
    return new CoordinatePoint(locationLat, locationLon);
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
    return "TripTimePrediction(id=" + id + " blockId=" + blockId
        + " serviceDate=" + serviceDate + " time=" + time
        + " scheduleDeviation=" + scheduleDeviation + " vehicleId=" + vehicleId
        + ")";
  }

  public static class Builder {

    private AgencyAndId blockId;

    private AgencyAndId tripId;

    private long serviceDate;

    private long time;

    private double scheduleDeviation = Double.NaN;

    private double distanceAlongBlock = Double.NaN;

    private double locationLat = Double.NaN;

    private double locationLon = Double.NaN;

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

    public void setScheduleDeviation(double scheduleDeviation) {
      this.scheduleDeviation = scheduleDeviation;
    }

    public void setDistanceAlongBlock(double distanceAlongBlock) {
      this.distanceAlongBlock = distanceAlongBlock;
    }

    public void setLocationLat(double locationLat) {
      this.locationLat = locationLat;
    }

    public void setLocationLon(double locationLon) {
      this.locationLon = locationLon;
    }

    public void setLocation(CoordinatePoint location) {
      if (location == null) {
        this.locationLat = Double.NaN;
        this.locationLon = Double.NaN;
      } else {
        this.locationLat = location.getLat();
        this.locationLon = location.getLon();
      }
    }

    public BlockLocationRecord create() {
      return new BlockLocationRecord(this);
    }

    public void setVehicleId(AgencyAndId vehicleId) {
      this.vehicleId = vehicleId;
    }
  }
}

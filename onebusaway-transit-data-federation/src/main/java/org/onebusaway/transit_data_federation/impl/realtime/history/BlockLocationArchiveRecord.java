package org.onebusaway.transit_data_federation.impl.realtime.history;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.mappings.AgencyIdFieldMappingFactory;
import org.onebusaway.realtime.api.EVehiclePhase;

@Entity
@Table(name = "oba_block_location_records_historical")
@org.hibernate.annotations.Table(appliesTo = "oba_block_location_records_historical", indexes = {@Index(name = "oba_block_location_records_historical_trip", columnNames = {
    "trip_agencyId", "trip_id"})})
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "block_location_records.csv", fieldOrder = {
    "id", "block_id_agencyId", "block_id_id", "distance_along_block",
    "location_lat", "location_lon", "orientation", "phase",
    "schedule_deviation", "service_date", "status", "time",
    "timepoint_agencyId", "timepoint_agencyId", "timepoint_predicted_time",
    "timepoint_scheduled_time", "trip_id_agencyId", "trip_id_id",
    "vehicle_id_agencyId", "vehicle_id_id", "distance_along_trip"})
public class BlockLocationArchiveRecord {

  @Id
  private String id;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "block_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "block_id"))})
  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId blockId;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId tripId;

  private long serviceDate;

  private long time;

  @Column(nullable = true)
  private Double scheduleDeviation;

  @Column(nullable = true)
  private Double distanceAlongBlock;

  @Column(nullable = true)
  private Double distanceAlongTrip;

  @Column(nullable = true)
  private Double locationLat;

  @Column(nullable = true)
  private Double locationLon;

  @Column(nullable = true)
  private Double orientation;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "timepoint_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "timepoint_id"))})
  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId timepointId;

  private long timepointScheduledTime;

  private long timepointPredictedTime;

  /**
   * Custom Hibernate mapping so that the vehicle phase enum gets mapped to a
   * string as opposed to an integer, allowing for safe expansion of the enum in
   * the future and more legibility in the raw SQL. Additionally, the phase
   * string can be a little shorter than the default length.
   */
  @Type(type = "org.onebusaway.container.hibernate.EnumUserType", parameters = {@Parameter(name = "enumClassName", value = "org.onebusaway.realtime.api.EVehiclePhase")})
  @Column(length = 50)
  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private EVehiclePhase phase;

  private String status;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "vehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "vehicle_id"))})
  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId vehicleId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AgencyAndId getBlockId() {
    return blockId;
  }

  public void setBlockId(AgencyAndId blockId) {
    this.blockId = blockId;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public Double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public Double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(Double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public Double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(Double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  public Double getLocationLat() {
    return locationLat;
  }

  public void setLocationLat(Double locationLat) {
    this.locationLat = locationLat;
  }

  public Double getLocationLon() {
    return locationLon;
  }

  public void setLocationLon(Double locationLon) {
    this.locationLon = locationLon;
  }

  public Double getOrientation() {
    return orientation;
  }

  public void setOrientation(Double orientation) {
    this.orientation = orientation;
  }

  public AgencyAndId getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(AgencyAndId timepointId) {
    this.timepointId = timepointId;
  }

  public long getTimepointScheduledTime() {
    return timepointScheduledTime;
  }

  public void setTimepointScheduledTime(long timepointScheduledTime) {
    this.timepointScheduledTime = timepointScheduledTime;
  }

  public long getTimepointPredictedTime() {
    return timepointPredictedTime;
  }

  public void setTimepointPredictedTime(long timepointPredictedTime) {
    this.timepointPredictedTime = timepointPredictedTime;
  }

  public EVehiclePhase getPhase() {
    return phase;
  }

  public void setPhase(EVehiclePhase phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }
}

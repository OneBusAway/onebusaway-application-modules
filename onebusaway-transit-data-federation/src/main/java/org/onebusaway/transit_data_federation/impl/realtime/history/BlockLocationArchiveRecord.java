/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
import org.onebusaway.csv_entities.schema.EnumFieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFieldNameConvention;
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
@CsvFields(filename = "block_location_records.csv", fieldNameConvention = CsvFieldNameConvention.CAMEL_CASE)
public class BlockLocationArchiveRecord {

  @Id
  private String id;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "block_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "block_id"))})
  @CsvField(name = "block", mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId blockId;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  @CsvField(name = "trip", optional = true, mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId tripId;

  private long serviceDate;

  private long time;

  @CsvField(optional = true)
  @Column(nullable = true)
  private Double scheduleDeviation;

  @CsvField(optional = true)
  @Column(nullable = true)
  private Double distanceAlongBlock;

  @CsvField(optional = true)
  @Column(nullable = true)
  private Double distanceAlongTrip;

  @CsvField(optional = true)
  @Column(nullable = true)
  private Double locationLat;

  @CsvField(optional = true)
  @Column(nullable = true)
  private Double locationLon;

  @CsvField(optional = true)
  @Column(nullable = true)
  private Double orientation;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "timepoint_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "timepoint_id"))})
  @CsvField(name = "timepoint", optional = true, mapping = AgencyIdFieldMappingFactory.class)
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
  @CsvField(optional = true, mapping = EnumFieldMappingFactory.class)
  private EVehiclePhase phase;

  @CsvField(optional = true)
  private String status;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "vehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "vehicle_id"))})
  @CsvField(name = "vehicle", mapping = AgencyIdFieldMappingFactory.class)
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

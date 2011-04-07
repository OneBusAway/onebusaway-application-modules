package org.onebusaway.transit_data_federation.impl.realtime.history;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.mappings.AgencyIdFieldMappingFactory;

@CsvFields(filename = "block_location_records.csv", fieldOrder = {
    "id", "block_id_agencyId", "block_id_id", "distance_along_block",
    "location_lat", "location_lon", "orientation", "phase",
    "schedule_deviation", "service_date", "status", "time",
    "timepoint_agencyId", "timepoint_agencyId", "timepoint_predicted_time",
    "timepoint_scheduled_time", "trip_id_agencyId", "trip_id_id",
    "vehicle_id_agencyId", "vehicle_id_id", "distance_along_trip"})
public class BlockLocationArchiveRecord {

  private String id;

  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId blockId;

  private double distanceAlongBlock;

  private int scheduleDeviation;

  private long serviceDate;

  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId tripId;

  @CsvField(mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId vehicleId;

  private double distanceAlongTrip;

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

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  public double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }
}

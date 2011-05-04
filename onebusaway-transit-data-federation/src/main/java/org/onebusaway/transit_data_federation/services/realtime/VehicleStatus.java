package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;

public class VehicleStatus {

  private VehicleLocationRecord record;

  private List<VehicleLocationRecord> allRecords;

  public VehicleLocationRecord getRecord() {
    return record;
  }

  public void setRecord(VehicleLocationRecord record) {
    this.record = record;
  }

  public AgencyAndId getVehicleId() {
    return record.getVehicleId();
  }

  public List<VehicleLocationRecord> getAllRecords() {
    return allRecords;
  }

  public void setAllRecords(List<VehicleLocationRecord> allRecords) {
    this.allRecords = allRecords;
  }
}

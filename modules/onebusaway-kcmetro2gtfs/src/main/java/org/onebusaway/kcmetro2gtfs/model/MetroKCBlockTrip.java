package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;

@CsvFields(filename = "block_trips.csv")
public class MetroKCBlockTrip {

  private String changeDate;

  private int blockId;

  private int tripId;

  private int tripSequence;

  public String getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }

  public int getBlockId() {
    return blockId;
  }

  public void setBlockId(int blockId) {
    this.blockId = blockId;
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
    this.tripId = tripId;
  }

  public int getTripSequence() {
    return tripSequence;
  }

  public void setTripSequence(int tripSequence) {
    this.tripSequence = tripSequence;
  }
  
  public ServicePatternKey getFullTripId() {
    return new ServicePatternKey(changeDate,tripId);
  }
}

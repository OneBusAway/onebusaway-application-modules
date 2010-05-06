package org.onebusaway.metrokc2gtfs.model;

import org.onebusaway.csv.CsvFields;

@CsvFields(filename = "block_trips.csv")
public class MetroKCBlockTrip {

  private int blockId;

  private int tripId;

  private int tripSequence;

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
}

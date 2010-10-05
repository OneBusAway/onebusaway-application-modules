package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public class BlockConfigurationEntryImpl implements BlockConfigurationEntry,
    Serializable {

  private static final long serialVersionUID = 1L;

  private final BlockEntry block;

  private final ServiceIdActivation serviceIds;

  private final List<BlockTripEntry> trips;

  private final double totalBlockDistance;

  private final BlockStopTimeList blockStopTimes = new BlockStopTimeList();

  private final int[] tripIndices;

  private final int[] accumulatedStopTimeIndices;

  public BlockConfigurationEntryImpl(Builder builder) {
    this.block = builder.block;
    this.serviceIds = builder.serviceIds;
    this.trips = builder.computeBlockTrips(this);
    this.totalBlockDistance = builder.computeTotalBlockDistance();
    this.tripIndices = builder.computeTripIndices();
    this.accumulatedStopTimeIndices = builder.computeAccumulatedStopTimeIndices();
  }

  public static Builder builder() {
    return new Builder();
  }

  /****
   * {@link BlockConfigurationEntry} Interface
   ****/

  @Override
  public BlockEntry getBlock() {
    return block;
  }

  @Override
  public ServiceIdActivation getServiceIds() {
    return serviceIds;
  }

  @Override
  public List<BlockTripEntry> getTrips() {
    return trips;
  }

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    return blockStopTimes;
  }

  @Override
  public double getTotalBlockDistance() {
    return totalBlockDistance;
  }

  @Override
  public String toString() {
    return "BlockConfiguration [serviceIds=" + serviceIds + " trips=" + trips
        + " stopTimes=" + blockStopTimes + "]";
  }

  public static class Builder {

    private BlockEntry block;

    private ServiceIdActivation serviceIds;

    private List<TripEntry> trips;

    private Builder() {

    }

    public void setBlock(BlockEntry block) {
      this.block = block;
    }

    public void setServiceIds(ServiceIdActivation serviceIds) {
      this.serviceIds = serviceIds;
    }

    public void setTrips(List<TripEntry> trips) {
      this.trips = trips;
    }

    public BlockConfigurationEntry create() {
      return new BlockConfigurationEntryImpl(this);
    }

    private double computeTotalBlockDistance() {
      double distance = 0;
      for (TripEntry trip : trips)
        distance += trip.getTotalTripDistance();
      return distance;
    }

    private int[] computeTripIndices() {
      int n = 0;
      for (TripEntry trip : trips)
        n += trip.getStopTimes().size();
      int[] tripIndices = new int[n];
      int index = 0;
      for (int tripIndex = 0; tripIndex < trips.size(); tripIndex++) {
        TripEntry trip = trips.get(tripIndex);
        for (int i = 0; i < trip.getStopTimes().size(); i++)
          tripIndices[index++] = tripIndex;
      }
      return tripIndices;
    }

    private int[] computeAccumulatedStopTimeIndices() {
      int[] accumulatedStopTimeIndices = new int[trips.size()];
      int n = 0;
      for (int i = 0; i < trips.size(); i++) {
        accumulatedStopTimeIndices[i] = n;
        n += trips.get(i).getStopTimes().size();
      }
      return accumulatedStopTimeIndices;
    }

    private List<BlockTripEntry> computeBlockTrips(
        BlockConfigurationEntryImpl blockConfiguration) {

      ArrayList<BlockTripEntry> blockTrips = new ArrayList<BlockTripEntry>();
      int accumulatedStopTimeIndex = 0;
      int accumulatedSlackTime = 0;
      double distanceAlongBlock = 0;
      BlockTripEntryImpl prev = null;

      for (int i = 0; i < trips.size(); i++) {

        TripEntry tripEntry = trips.get(i);

        BlockTripEntryImpl blockTripEntry = new BlockTripEntryImpl();
        blockTripEntry.setTrip(tripEntry);
        blockTripEntry.setBlockConfiguration(blockConfiguration);
        blockTripEntry.setAccumulatedStopTimeIndex(accumulatedStopTimeIndex);
        blockTripEntry.setAccumulatedSlackTime(accumulatedSlackTime);
        blockTripEntry.setDistanceAlongBlock(distanceAlongBlock);

        if (prev != null) {
          prev.setNextTrip(blockTripEntry);
          blockTripEntry.setPreviousTrip(prev);
        }

        blockTrips.add(blockTripEntry);

        List<StopTimeEntry> stopTimes = tripEntry.getStopTimes();
        accumulatedStopTimeIndex += stopTimes.size();
        for (StopTimeEntry stopTime : stopTimes)
          accumulatedSlackTime += stopTime.getSlackTime();
        distanceAlongBlock += tripEntry.getTotalTripDistance();

        prev = blockTripEntry;
      }

      blockTrips.trimToSize();

      return blockTrips;
    }
  }

  private class BlockStopTimeList extends AbstractList<BlockStopTimeEntry>
      implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int size() {
      return tripIndices.length;
    }

    @Override
    public BlockStopTimeEntry get(int index) {

      int tripIndex = tripIndices[index];

      BlockTripEntry blockTrip = trips.get(tripIndex);
      TripEntry trip = blockTrip.getTrip();

      List<StopTimeEntry> stopTimes = trip.getStopTimes();
      int stopTimeIndex = index - accumulatedStopTimeIndices[tripIndex];
      StopTimeEntry stopTime = stopTimes.get(stopTimeIndex);

      return new BlockStopTimeEntryImpl(stopTime, index, blockTrip);
    }
  }

}

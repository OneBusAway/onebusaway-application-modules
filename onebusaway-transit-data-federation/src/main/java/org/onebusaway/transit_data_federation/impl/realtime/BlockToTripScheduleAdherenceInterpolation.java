package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehiclePositionRecord;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockToTripScheduleAdherenceInterpolation {

  private TransitGraphDao _transitGraphDao;

  private int _blockTimeBefore = 10 * 60;

  private int _blockTimeAfter = 30 * 60;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  public List<VehiclePositionRecord> interpolate(
      VehiclePositionRecord record) {

    long serviceDate = record.getServiceDate();
    long currentTime = record.getCurrentTime();
    int serviceTime = (int) ((currentTime - serviceDate) / 1000);

    serviceTime -= record.getScheduleDeviation();

    int timeFrom = serviceTime - _blockTimeBefore;
    int timeTo = serviceTime + _blockTimeAfter;

    List<TripEntry> trips = getTripsInTimeRangeForBlock(record.getBlockId(),
        timeFrom, timeTo);

    List<VehiclePositionRecord> records = new ArrayList<VehiclePositionRecord>(
        trips.size());

    for (TripEntry trip : trips) {
      VehiclePositionRecord r = new VehiclePositionRecord(record);
      r.setTripId(trip.getId());
      records.add(r);
    }

    return records;
  }

  public List<TripEntry> getTripsInTimeRangeForBlock(AgencyAndId blockId,
      int from, int to) {
    List<TripEntry> tripsForBlockId = _transitGraphDao.getTripsForBlockId(blockId);

    TripEntryList maxTime = new TripEntryList(tripsForBlockId, false);
    int fromIndex = fixIndex(Collections.binarySearch(maxTime, from));

    TripEntryList minTime = new TripEntryList(tripsForBlockId, true);
    int toIndex = fixIndex(Collections.binarySearch(minTime, to));

    List<TripEntry> inRange = new ArrayList<TripEntry>();
    for (int index = fromIndex; index < toIndex; index++)
      inRange.add(tripsForBlockId.get(index));
    return inRange;
  }

  /****
   * Private Methods
   ****/

  private static final int fixIndex(int index) {
    if (index < 0)
      index = -(index + 1);
    return index;
  }

  private static class TripEntryList extends AbstractList<Integer> {

    private List<TripEntry> _source;
    private boolean _isMin;

    public TripEntryList(List<TripEntry> source, boolean isMin) {
      _source = source;
      _isMin = isMin;
    }

    @Override
    public Integer get(int index) {
      TripEntry tripEntry = _source.get(index);
      List<StopTimeEntry> stopTimes = tripEntry.getStopTimes();
      if (_isMin) {
        StopTimeEntry first = stopTimes.get(0);
        return first.getArrivalTime();
      } else {
        StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);
        return last.getDepartureTime();
      }
    }

    @Override
    public int size() {
      return _source.size();
    }
  }

}

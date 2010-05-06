package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.MetroKCStopTime;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;
import org.onebusaway.kcmetro2gtfs.model.TimepointAndIndex;

import edu.washington.cs.rse.collections.FactoryMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopTimeHandler extends InputHandler {

  private static String[] STOP_TIME_FIELDS = {
      "change_date", "trip_id", "stop_time_position", "db_mod_date",
      "passing_time", "service_pattern_id", "timepoint",
      "pattern_timepoint_position", "first_last_flag"};

  private Map<ServicePatternKey, Map<TimepointAndIndex, MetroKCStopTime>> _stopTimesByTrip = new FactoryMap<ServicePatternKey, Map<TimepointAndIndex, MetroKCStopTime>>(
      new HashMap<TimepointAndIndex, MetroKCStopTime>());

  private Set<ServicePatternKey> _tripsToSkip = new HashSet<ServicePatternKey>();

  private TranslationContext _context;

  public StopTimeHandler(TranslationContext context) {
    super(MetroKCStopTime.class, STOP_TIME_FIELDS);
    _context = context;
  }

  public Map<TimepointAndIndex, MetroKCStopTime> getStopTimesByTripId(
      ServicePatternKey tripId) {
    return _stopTimesByTrip.get(tripId);
  }

  public Set<ServicePatternKey> getTripsToSkip() {
    return _tripsToSkip;
  }

  public void handleEntity(Object bean) {

    MetroKCStopTime st = (MetroKCStopTime) bean;
    TimepointAndIndex key = new TimepointAndIndex(st.getTimepoint(),
        st.getPatternTimepointPosition());
    _stopTimesByTrip.get(st.getFullTripId()).put(key, st);
  }

  @Override
  public void close() {
    super.close();

    for (Map.Entry<ServicePatternKey, Map<TimepointAndIndex, MetroKCStopTime>> entry : _stopTimesByTrip.entrySet()) {

      Map<TimepointAndIndex, MetroKCStopTime> stopTimesByTimepointAndIndex = entry.getValue();

      if (stopTimesByTimepointAndIndex.size() > 1
          && areAllTimepointIndicesZero(stopTimesByTimepointAndIndex.keySet())) {

        ServicePatternKey tripId = entry.getKey();
        _context.addWarning("pattern_timepoint_position problem: tripId="
            + tripId);
        _tripsToSkip.add(tripId);

        List<TimepointAndIndex> indices = new ArrayList<TimepointAndIndex>(
            stopTimesByTimepointAndIndex.keySet());
        Collections.sort(indices, new ThisComparator(
            stopTimesByTimepointAndIndex));
        int index = 0;
        for (TimepointAndIndex ti : indices) {
          TimepointAndIndex newTi = new TimepointAndIndex(ti.getTimepoint(),
              index++);
          MetroKCStopTime stopTime = stopTimesByTimepointAndIndex.remove(ti);
          stopTimesByTimepointAndIndex.put(newTi, stopTime);
        }
      }

      Set<Integer> indices = new HashSet<Integer>();

      for (TimepointAndIndex timepointAndIndex : stopTimesByTimepointAndIndex.keySet()) {
        if (!indices.add(timepointAndIndex.getIndex())) {
          throw new IllegalStateException(
              "duplicate pattern_timepoint_position in stop_times for trip="
                  + entry.getKey());
        }
      }
    }
  }

  private boolean areAllTimepointIndicesZero(
      Iterable<TimepointAndIndex> timepointIndices) {
    for (TimepointAndIndex timepointIndex : timepointIndices) {
      if (timepointIndex.getIndex() != 0)
        return false;
    }
    return true;
  }

  private static class ThisComparator implements Comparator<TimepointAndIndex> {

    private Map<TimepointAndIndex, MetroKCStopTime> _stopTimesByTimepointAndIndex;

    public ThisComparator(
        Map<TimepointAndIndex, MetroKCStopTime> stopTimesByTimepointAndIndex) {
      _stopTimesByTimepointAndIndex = stopTimesByTimepointAndIndex;
    }

    public int compare(TimepointAndIndex o1, TimepointAndIndex o2) {
      MetroKCStopTime st1 = _stopTimesByTimepointAndIndex.get(o1);
      MetroKCStopTime st2 = _stopTimesByTimepointAndIndex.get(o2);
      return Double.compare(st1.getPassingTime(), st2.getPassingTime());
    }
  }
}

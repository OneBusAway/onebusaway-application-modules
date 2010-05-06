package org.onebusaway.metrokc2gtfs.handlers;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.metrokc2gtfs.model.MetroKCStopTime;
import org.onebusaway.metrokc2gtfs.model.TimepointAndIndex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StopTimeHandler extends InputHandler {

  private static String[] STOP_TIME_FIELDS = {
      "change_date", "trip_id", "stop_time_position", "db_mod_date", "passing_time", "service_pattern_id", "timepoint",
      "pattern_timepoint_position", "first_last_flag"};

  private Map<Integer, Map<TimepointAndIndex, MetroKCStopTime>> _stopTimesByTrip = new FactoryMap<Integer, Map<TimepointAndIndex, MetroKCStopTime>>(
      new HashMap<TimepointAndIndex, MetroKCStopTime>());

  public StopTimeHandler() {
    super(MetroKCStopTime.class, STOP_TIME_FIELDS);
  }

  public Map<TimepointAndIndex, MetroKCStopTime> getStopTimesByTripId(int tripId) {
    return _stopTimesByTrip.get(tripId);
  }

  public void handleEntity(Object bean) {

    MetroKCStopTime st = (MetroKCStopTime) bean;
    TimepointAndIndex key = new TimepointAndIndex(st.getTimepoint(), st.getPatternTimepointPosition());
    _stopTimesByTrip.get(st.getTripId()).put(key, st);
  }

  @Override
  public void close() {
    super.close();

    for (Map.Entry<Integer, Map<TimepointAndIndex, MetroKCStopTime>> entry : _stopTimesByTrip.entrySet()) {
      Map<TimepointAndIndex, MetroKCStopTime> stopTimesByTimepointAndIndex = entry.getValue();

      Set<Integer> indices = new HashSet<Integer>();

      for (TimepointAndIndex timepointAndIndex : stopTimesByTimepointAndIndex.keySet()) {
        if (!indices.add(timepointAndIndex.getIndex()))
          throw new IllegalStateException("duplicate pattern_timepoint_position in stop_times for trip="
              + entry.getKey());
      }
    }
  }
}

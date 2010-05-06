package org.onebusaway.metrokc2gtdf.handlers;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.metrokc2gtdf.model.MetroKCStopTime;
import org.onebusaway.metrokc2gtdf.model.TimepointAndIndex;

import java.util.HashMap;
import java.util.Map;

public class StopTimeHandler extends InputHandler {

  private static String[] STOP_TIME_FIELDS = {
      "change_date", "trip_id", "stop_time_position", "db_mod_date",
      "passing_time", "service_pattern_id", "timepoint",
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
    TimepointAndIndex key = new TimepointAndIndex(st.getTimepoint(),
        st.getPatternTimepointPosition());
    _stopTimesByTrip.get(st.getTripId()).put(key, st);
  }
}

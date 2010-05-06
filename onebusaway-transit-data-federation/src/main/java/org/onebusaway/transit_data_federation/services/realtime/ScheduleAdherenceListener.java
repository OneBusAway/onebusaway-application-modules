package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

public interface ScheduleAdherenceListener {
  public void handleScheduleAdherenceRecords(List<ScheduleAdherenceRecord> records);
}

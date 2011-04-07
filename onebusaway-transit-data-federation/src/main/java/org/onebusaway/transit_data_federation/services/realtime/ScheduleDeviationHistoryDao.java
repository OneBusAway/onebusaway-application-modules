package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.history.ScheduleDeviationHistory;

public interface ScheduleDeviationHistoryDao {

  public void saveScheduleDeviationHistory(ScheduleDeviationHistory record);

  public void saveScheduleDeviationHistory(
      List<ScheduleDeviationHistory> records);

  public ScheduleDeviationHistory getScheduleDeviationHistoryForTripId(
      AgencyAndId tripId);
}

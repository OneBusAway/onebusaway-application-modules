/**
 * 
 */
package org.onebusaway.transit_data_federation.model.predictions;

import java.io.Serializable;

import org.onebusaway.transit_data_federation.services.realtime.ScheduleAdherenceRecord;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public final class ScheduleAdherenceEntries implements Serializable {

  private static final int RECORD_TIMEOUT = 7 * 60 * 1000;

  private static final long serialVersionUID = 1L;

  private ScheduleAdherenceRecord _record = null;

  public void addPredictions(ScheduleAdherenceRecord record) {
    _record = record;
  }

  /**
   * 
   * @param sti
   * @return true if a prediction was applied, otherwise false
   */
  public boolean applyPredictions(StopTimeInstanceProxy sti) {

    if (_record == null)
      return false;
    
    if (_record.getCurrentTime() + RECORD_TIMEOUT < System.currentTimeMillis()) {
      _record = null;
      return false;
    }

    sti.setPredictedArrivalOffset(_record.getScheduleDeviation());
    sti.setPredictedDepartureOffset(_record.getScheduleDeviation());

    return true;
  }
}
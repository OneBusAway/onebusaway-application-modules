package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.AgencyAndIdInstance;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;

public interface BlockLocationHistoryService {
  public Map<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> getHistoryForTripId(
      AgencyAndId tripId);
}

package org.onebusaway.transit_data_federation.bundle.tasks.history;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;

public interface BlockLocationArchiveSource {
  public List<BlockLocationArchiveRecord> getRecordsForTrip(AgencyAndId blockId);
}

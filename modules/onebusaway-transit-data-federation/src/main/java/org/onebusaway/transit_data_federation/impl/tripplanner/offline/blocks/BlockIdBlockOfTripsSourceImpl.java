package org.onebusaway.transit_data_federation.impl.tripplanner.offline.blocks;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;

import java.util.List;

public class BlockIdBlockOfTripsSourceImpl implements BlockOfTripsSource {

  private AgencyAndId _blockId;

  public BlockIdBlockOfTripsSourceImpl(AgencyAndId blockId) {
    _blockId = blockId;
  }

  public List<Trip> getTrips(ExtendedGtfsRelationalDao gtfsDao) {
    return gtfsDao.getTripsForBlockId(_blockId);
  }

  public List<StopTime> getStopTimes(ExtendedGtfsRelationalDao gtfsDao) {
    return gtfsDao.getStopTimesForBlockId(_blockId);
  }
}

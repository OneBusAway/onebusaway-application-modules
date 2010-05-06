package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StopDifferenceServiceImpl extends AbstractDifferenceServiceImpl
    implements GtfsDifferenceService {

  public void computeDifferences() {

    Collection<Stop> stops = _gtfsDao.getAllStops();

    Map<AgencyAndId, Stop> stopsById = CollectionsLibrary.mapToValue(stops,
        "id", AgencyAndId.class);

    Map<AgencyAndId, Stop> stopsInAById = translateIds(stopsById,
        _results.getModelIdA());
    Map<AgencyAndId, Stop> stopsInBById = translateIds(stopsById,
        _results.getModelIdB());

    Set<AgencyAndId> commonIds = new HashSet<AgencyAndId>(stopsInAById.keySet());
    commonIds.retainAll(stopsInBById.keySet());

    for (AgencyAndId id : commonIds) {
      Stop stopA = stopsInAById.remove(id);
      Stop stopB = stopsInBById.remove(id);
      EntityMatch<Stop> match = _results.addMatch(new EntityMatch<Stop>(stopA, stopB));
      computeEntityPropertyDifferences(stopA, stopB, match, "id");
    }

    for (Stop stopA : stopsInAById.values()) {
      _results.addMismatch(new EntityMismatch(stopA, null));
    }

    for (Stop stopB : stopsInBById.values()) {
      _results.addMismatch(new EntityMismatch(null, stopB));
    }
  }
}

package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AgencyDifferenceServiceImpl extends AbstractDifferenceServiceImpl
    implements GtfsDifferenceService {

  public void computeDifferences() {

    Collection<Agency> agencies = _gtfsDao.getAllAgencies();

    Map<String, Agency> agenciesById = CollectionsLibrary.mapToValue(agencies,
        "id", String.class);

    Map<String, Agency> agenciesInAById = translateIds(agenciesById,
        _results.getModelIdA());
    Map<String, Agency> agenciesInBById = translateIds(agenciesById,
        _results.getModelIdB());

    Set<String> commonIds = new HashSet<String>(agenciesInAById.keySet());
    commonIds.retainAll(agenciesInBById.keySet());

    for (String agencyId : commonIds) {
      Agency agencyA = agenciesInAById.remove(agencyId);
      Agency agencyB = agenciesInBById.remove(agencyId);
      _results.addMatch(new EntityMatch<Agency>(agencyA, agencyB));
      computeEntityPropertyDifferences(agencyA, agencyB, _results, "id");
    }

    for (Agency agencyA : agenciesInAById.values()) {
      _results.addMismatch(new EntityMismatch(agencyA, null));
    }

    for (Agency agencyB : agenciesInBById.values()) {
      _results.addMismatch(new EntityMismatch(null, agencyB));
    }
  }
}

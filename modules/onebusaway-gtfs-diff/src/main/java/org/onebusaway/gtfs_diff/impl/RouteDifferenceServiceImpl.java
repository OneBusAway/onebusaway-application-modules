package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RouteDifferenceServiceImpl extends AbstractDifferenceServiceImpl
    implements GtfsDifferenceService {

  public void computeDifferences() {

    List<EntityMatch<Agency>> agencyMatches = getEntityMatches(_results,
        Agency.class);

    for (EntityMatch<Agency> agencyMatch : agencyMatches) {

      List<Route> routesA = _gtfsDao.getRoutesForAgency(agencyMatch.getEntityA());
      List<Route> routesB = _gtfsDao.getRoutesForAgency(agencyMatch.getEntityB());

      Map<AgencyAndId, Route> routesByIdA = mapAndTranlateIds(routesA, "id",
          AgencyAndId.class, _results.getModelIdA());
      Map<AgencyAndId, Route> routesByIdB = mapAndTranlateIds(routesB, "id",
          AgencyAndId.class, _results.getModelIdB());

      Set<AgencyAndId> commonIds = new HashSet<AgencyAndId>(
          routesByIdA.keySet());
      commonIds.retainAll(routesByIdB.keySet());

      for (AgencyAndId id : commonIds) {
        Route routeA = routesByIdA.remove(id);
        Route routeB = routesByIdB.remove(id);
        EntityMatch<Route> match = agencyMatch.addMatch(new EntityMatch<Route>(
            routeA, routeB));
        computeEntityPropertyDifferences(routeA, routeB, match, "id", "agency");
      }

      for (Route routeA : routesByIdA.values())
        agencyMatch.addMismatch(new EntityMismatch(routeA, null));

      for (Route routeB : routesByIdB.values())
        agencyMatch.addMismatch(new EntityMismatch(null, routeB));
    }
  }
}

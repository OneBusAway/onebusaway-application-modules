package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.Collection;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(StopEntriesFactory.class);

  private GtfsRelationalDao _gtfsDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  /**
   * Iterate over each stop, generating a StopEntry for the graph.
   * 
   * @param graph
   */
  public void processStops(TransitGraphImpl graph) {

    int stopIndex = 0;

    Collection<Stop> stops = _gtfsDao.getAllStops();

    for (Stop stop : stops) {

      if (stopIndex % 500 == 0)
        _log.info("stops: " + stopIndex + "/" + stops.size());
      stopIndex++;

      StopEntryImpl stopEntry = new StopEntryImpl(stop.getId(), stop.getLat(),
          stop.getLon());
      graph.putStopEntry(stopEntry);
    }

    graph.refreshStopMapping();
  }
}

package org.onebusaway.transit_data_federation.impl;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransitGraphDaoImpl implements TransitGraphDao {

  private TripPlannerGraph _graph;

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  /****
   * {@link TransitGraphDao} Interface
   ****/

  @Override
  public Iterable<StopEntry> getAllStops() {
    return _graph.getAllStops();
  }

  @Override
  public StopEntry getStopEntryForId(AgencyAndId id) {
    return _graph.getStopEntryForId(id);
  }

  @Override
  public List<StopEntry> getStopsByLocation(CoordinateBounds bounds) {
    return _graph.getStopsByLocation(bounds);
  }

  @Override
  public Iterable<BlockEntry> getAllBlocks() {
    return _graph.getAllBlocks();
  }

  @Override
  public BlockEntry getBlockEntryForId(AgencyAndId blockId) {
    return _graph.getBlockEntryForId(blockId);
  }

  @Override
  public Iterable<TripEntry> getAllTrips() {
    return _graph.getAllTrips();
  }

  @Override
  public TripEntry getTripEntryForId(AgencyAndId id) {
    return _graph.getTripEntryForId(id);
  }
}

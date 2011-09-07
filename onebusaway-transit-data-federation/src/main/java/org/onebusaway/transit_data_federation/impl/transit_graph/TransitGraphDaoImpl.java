package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.refresh.RefreshableResources;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraph;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransitGraphDaoImpl implements TransitGraphDao {

  private FederatedTransitDataBundle _bundle;

  private TransitGraph _graph;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void setTransitGraph(TransitGraph graph) {
    _graph = graph;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {
    File path = _bundle.getTransitGraphPath();
    if (path.exists()) {
      TransitGraphImpl graph = ObjectSerializationLibrary.readObject(path);
      graph.initialize();
      _graph = graph;
    } else {
      _graph = new TransitGraphImpl();
    }
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
  public Iterable<StopEntry> getStopsByLocation(CoordinateBounds bounds) {
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

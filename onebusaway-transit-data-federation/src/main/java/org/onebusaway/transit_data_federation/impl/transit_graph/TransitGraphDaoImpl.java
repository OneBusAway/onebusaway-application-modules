/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.transit_graph.DynamicGraph;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.model.transit_graph.TransitGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransitGraphDaoImpl implements TransitGraphDao {

  private static final Logger _log = LoggerFactory.getLogger(TransitGraphDaoImpl.class);
  private FederatedTransitDataBundle _bundle;

  private TransitGraph _graph;

  private DynamicGraph _dynamicGraph;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setDynamicGraph(DynamicGraph dynamicGraph) {
    _dynamicGraph = dynamicGraph;
  }
  public void setTransitGraph(TransitGraph graph) {
    _graph = graph;
  }

  private boolean changingBundle = true;

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {
    changingBundle = true;
    File path = _bundle.getTransitGraphPath();

    if(_graph != null) {
      TransitGraphImpl graph = (TransitGraphImpl)_graph;
      graph.empty();
      _graph = null;
    }
    
    if (path.exists()) {
      TransitGraphImpl graph = ObjectSerializationLibrary.readObject(path);
      graph.initialize();
      _graph = graph;
    } else {
      _graph = new TransitGraphImpl();
    }
    changingBundle = false;
  }

  private void blockUntilGraphIsReady() {
    int i = 0;
    while (changingBundle) {
      try {
        Thread.sleep(250);
        Thread.yield(); // let some other work be done
      } catch (InterruptedException e) {
        return;
      }
      i++;
      if (i % 100 == 0) {
        _log.warn("Bundle is not ready or none is loaded--we've blocked 100 Graph requests since last log event.");
      }
    }
  }

  /****
   * {@link TransitGraphDao} Interface
   ****/

  @Override
  public List<AgencyEntry> getAllAgencies() {
    blockUntilGraphIsReady();
    return _graph.getAllAgencies();
  }

  @Override
  public AgencyEntry getAgencyForId(String id) {
    blockUntilGraphIsReady();
    return _graph.getAgencyForId(id);
  }

  @Override
  public List<StopEntry> getAllStops() {
    blockUntilGraphIsReady();
    return _graph.getAllStops();
  }

  @Override
  public StopEntry getStopEntryForId(AgencyAndId id) {
    blockUntilGraphIsReady();
    return _graph.getStopEntryForId(id);
  }

  @Override
  public StopEntry getStopEntryForId(AgencyAndId id,
      boolean throwExceptionIfNotFound) {
    blockUntilGraphIsReady();
    StopEntry stop = _graph.getStopEntryForId(id);
    if (stop == null && throwExceptionIfNotFound)
      throw new NoSuchStopServiceException(
          AgencyAndIdLibrary.convertToString(id));
    return stop;
  }

  @Override
  public List<StopEntry> getStopsByLocation(CoordinateBounds bounds) {
    blockUntilGraphIsReady();
    return _graph.getStopsByLocation(bounds);
  }

  @Override
  public List<BlockEntry> getAllBlocks() {
    blockUntilGraphIsReady();
    return _graph.getAllBlocks();
  }

  @Override
  public BlockEntry getBlockEntryForId(AgencyAndId blockId) {
    blockUntilGraphIsReady();
    return _graph.getBlockEntryForId(blockId);
  }

  @Override
  public List<TripEntry> getAllTrips() {
    blockUntilGraphIsReady();
    return _graph.getAllTrips();
  }

  @Override
  public TripEntry getTripEntryForId(AgencyAndId id) {
    blockUntilGraphIsReady();
    TripEntry entry = _graph.getTripEntryForId(id);
    if (entry == null && _dynamicGraph != null)
      entry = _dynamicGraph.getTripEntryForId(id);
    return entry;
  }

  @Override
  public List<RouteCollectionEntry> getAllRouteCollections() {
    blockUntilGraphIsReady();
    return _graph.getAllRouteCollections();
  }

  @Override
  public RouteCollectionEntry getRouteCollectionForId(AgencyAndId id) {
    blockUntilGraphIsReady();
    return _graph.getRouteCollectionForId(id);
  }

  @Override
  public List<RouteEntry> getAllRoutes() {
    blockUntilGraphIsReady();
    return _graph.getAllRoutes();
  }

  @Override
  public RouteEntry getRouteForId(AgencyAndId id) {
    blockUntilGraphIsReady();
    return _graph.getRouteForId(id);
  }
}

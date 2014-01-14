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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;

public class TransitGraphTaskTest {

  @Test
  public void testInterpolation() throws IOException, ClassNotFoundException {

    AgencyEntriesFactory agencyEntriesFactory = Mockito.mock(AgencyEntriesFactory.class);
    BlockEntriesFactory blockEntriesFactory = Mockito.mock(BlockEntriesFactory.class);
    RouteCollectionEntriesFactory routeCollectionEntriesFactory = Mockito.mock(RouteCollectionEntriesFactory.class);
    RouteEntriesFactory routeEntriesFactory = Mockito.mock(RouteEntriesFactory.class);
    StopEntriesFactory stopEntriesFactory = Mockito.mock(StopEntriesFactory.class);
    TripEntriesFactory tripEntriesFactory = Mockito.mock(TripEntriesFactory.class);
    FrequencyEntriesFactory frequencyEntriesFactory = Mockito.mock(FrequencyEntriesFactory.class);
    RefreshService refreshService = Mockito.mock(RefreshService.class);

    TransitGraphTask task = new TransitGraphTask();
    task.setAgencyEntriesFactory(agencyEntriesFactory);
    task.setBlockEntriesFactory(blockEntriesFactory);
    task.setRouteCollectionEntriesFactroy(routeCollectionEntriesFactory);
    task.setRouteEntriesFactory(routeEntriesFactory);
    task.setStopEntriesFactory(stopEntriesFactory);
    task.setTripEntriesFactory(tripEntriesFactory);
    task.setFrequencyEntriesFactory(frequencyEntriesFactory);
    task.setRefreshService(refreshService);

    File path = File.createTempFile("TemporaryBundleDirectory-", "");
    path.delete();

    FederatedTransitDataBundle bundle = Mockito.mock(FederatedTransitDataBundle.class);
    Mockito.when(bundle.getTransitGraphPath()).thenReturn(path);

    task.setBundle(bundle);

    task.run();

    Mockito.verify(agencyEntriesFactory).processAgencies(
        Mockito.any(TransitGraphImpl.class));
    Mockito.verify(routeEntriesFactory).processRoutes(
        Mockito.any(TransitGraphImpl.class));
    Mockito.verify(routeCollectionEntriesFactory).processRouteCollections(
        Mockito.any(TransitGraphImpl.class));
    Mockito.verify(stopEntriesFactory).processStops(
        Mockito.any(TransitGraphImpl.class));
    Mockito.verify(tripEntriesFactory).processTrips(
        Mockito.any(TransitGraphImpl.class));

    Mockito.verify(refreshService).refresh(RefreshableResources.TRANSIT_GRAPH);

    assertTrue(path.exists());

    TransitGraphImpl graph = ObjectSerializationLibrary.readObject(path);
    assertNotNull(graph);
  }
}

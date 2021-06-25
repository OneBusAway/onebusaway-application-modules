/**
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;

public class ShapeGeospatialIndexTaskTest {

  @Test
  public void test() throws IOException, ClassNotFoundException {

    ShapeGeospatialIndexTask task = new ShapeGeospatialIndexTask();

    File path = File.createTempFile(
        ShapeGeospatialIndexTaskTest.class.getName(), ".tmp");
    path.delete();
    path.deleteOnExit();
    FederatedTransitDataBundle bundle = Mockito.mock(FederatedTransitDataBundle.class);
    Mockito.when(bundle.getShapeGeospatialIndexDataPath()).thenReturn(path);
    task.setBundle(bundle);

    RefreshService refreshService = Mockito.mock(RefreshService.class);
    task.setRefreshService(refreshService);

    ShapePointHelper shapePointHelper = Mockito.mock(ShapePointHelper.class);
    task.setShapePointHelper(shapePointHelper);

    TransitGraphDao transitGraphDao = Mockito.mock(TransitGraphDao.class);
    task.setTransitGraphDao(transitGraphDao);

    StopEntry stopA = stop("stopA", 47.65, -122.32);
    StopEntry stopB = stop("stopB", 47.67, -122.30);

    Mockito.when(transitGraphDao.getAllStops()).thenReturn(
        Arrays.asList(stopA, stopB));

    TripEntryImpl tripA = trip("tripA");
    AgencyAndId shapeIdA = aid("shapeA");
    tripA.setShapeId(shapeIdA);
    TripEntryImpl tripB = trip("tripB");
    AgencyAndId shapeIdB = aid("shapeB");
    tripB.setShapeId(shapeIdB);

    Mockito.when(transitGraphDao.getAllTrips()).thenReturn(
        Arrays.asList((TripEntry) tripA, tripB));

    ShapePointsFactory factory = new ShapePointsFactory();
    factory.addPoint(47.652300128129454, -122.30622018270873);
    factory.addPoint(47.653181844549394, -122.30523312979125);
    factory.addPoint(47.654265901710744, -122.30511511259459);
    ShapePoints shapeA = factory.create();

    factory = new ShapePointsFactory();
    factory.addPoint(47.661275594717026, -122.31189573698424);
    factory.addPoint(47.661347854692465, -122.3240622370758);
    factory.addPoint(47.661368177792546, -122.32508885257624);
    factory.addPoint(47.66496659665593, -122.32501375072383);
    ShapePoints shapeB = factory.create();

    Mockito.when(shapePointHelper.getShapePointsForShapeId(shapeIdA)).thenReturn(
        shapeA);
    Mockito.when(shapePointHelper.getShapePointsForShapeId(shapeIdB)).thenReturn(
        shapeB);

    task.run();

    Mockito.verify(refreshService).refresh(
        RefreshableResources.SHAPE_GEOSPATIAL_INDEX);

    // results are 5 cells bounding the two shapes:
    //http://developer.onebusaway.org/images/ShapeGeospatialIndexTaskTest.png
    Map<CoordinateBounds, List<AgencyAndId>> shapeIdsByBounds = ObjectSerializationLibrary.readObject(path);
    assertEquals(5, shapeIdsByBounds.size());

//    CoordinateBounds b = new CoordinateBounds(47.65048049686506,
//        -122.30767397879845, 47.654977097836735, -122.300997795721);
    // in java11 the precision of this bounding box changed, perhaps due to changes in sin/cos calculation
    CoordinateBounds b = new CoordinateBounds(47.65048049678976,-122.30767397879845,
    47.65497709776143,-122.300997795721);
    assertEquals(shapeIdA.toString(), "1_shapeA");
    assertNotNull(shapeIdsByBounds.get(b));
    assertEquals(Arrays.asList(shapeIdA), shapeIdsByBounds.get(b));

//    b = new CoordinateBounds(47.65947369880841, -122.32102634495334,
//        47.66397029978009, -122.3143501618759);
    // in java11 the precision of this bounding box changed, perhaps due to changes in sin/cos calculation
    b = new CoordinateBounds(47.6594736987331,-122.32102634495334,
    47.66397029970477,-122.3143501618759);
    assertEquals(Arrays.asList(shapeIdB), shapeIdsByBounds.get(b));
//    b = new CoordinateBounds(47.66397029978009, -122.32770252803078,
//        47.66846690075177, -122.32102634495334);
    // in java11 the precision of this bounding box changed, perhaps due to changes in sin/cos calculation
    b = new CoordinateBounds(47.66397029970477,-122.32770252803078,
            47.66846690067644,-122.32102634495334);

    assertEquals(Arrays.asList(shapeIdB), shapeIdsByBounds.get(b));
//    b = new CoordinateBounds(47.65947369880841, -122.3143501618759,
//        47.66397029978009, -122.30767397879845);
    // in java11 the precision of this bounding box changed, perhaps due to changes in sin/cos calculation
    b = new CoordinateBounds(47.6594736987331,-122.3143501618759,
    47.66397029970477,-122.30767397879845);

    assertEquals(Arrays.asList(shapeIdB), shapeIdsByBounds.get(b));
//    b = new CoordinateBounds(47.65947369880841, -122.32770252803078,
//        47.66397029978009, -122.32102634495334);
    // in java11 the precision of this bounding box changed, perhaps due to changes in sin/cos calculation
    b = new CoordinateBounds(47.6594736987331,-122.32770252803078,
            47.66397029970477,-122.32102634495334);

    assertEquals(Arrays.asList(shapeIdB), shapeIdsByBounds.get(b));
  }
}

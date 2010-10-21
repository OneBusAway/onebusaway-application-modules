package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.shapePoint;

import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class ShapePointsTemporaryServiceTest {

  @Test
  public void test() {

    GtfsRelationalDaoImpl gtfsDao = new GtfsRelationalDaoImpl();

    ShapePointsTemporaryService shapePointsService = new ShapePointsTemporaryService();
    shapePointsService.setGtfsDao(gtfsDao);

    gtfsDao.saveEntity(shapePoint("a", 1, 47.6738, -122.3875));
    gtfsDao.saveEntity(shapePoint("a", 2, 47.6686, -122.3875));
    gtfsDao.saveEntity(shapePoint("a", 3, 47.6686, -122.3661));

    ShapePoints points = shapePointsService.getShapePoints(aid("a"));

    assertEquals(3, points.getSize());

    assertEquals(47.6738, points.getLats()[0], 0.0);
    assertEquals(47.6686, points.getLats()[1], 0.0);
    assertEquals(47.6686, points.getLats()[2], 0.0);

    assertEquals(-122.3875, points.getLons()[0], 0.0);
    assertEquals(-122.3875, points.getLons()[1], 0.0);
    assertEquals(-122.3661, points.getLons()[2], 0.0);
  }
  
  @Test
  public void testReturnsNullWhenNoShapePoints() {

    GtfsRelationalDaoImpl gtfsDao = new GtfsRelationalDaoImpl();

    ShapePointsTemporaryService shapePointsService = new ShapePointsTemporaryService();
    shapePointsService.setGtfsDao(gtfsDao);

    ShapePoints points = shapePointsService.getShapePoints(aid("a"));
    assertNull(points);
  }
}

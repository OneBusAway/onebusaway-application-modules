package org.onebusaway.tripplanner.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.junit.Test;

public class VehicleContinuationStateTest {

  private static GeometryFactory _geometryFactory = new GeometryFactory();

  @Test
  public void testEquals() {

    Point locationA = _geometryFactory.createPoint(new Coordinate(0, 1));
    Point locationB = _geometryFactory.createPoint(new Coordinate(1, 0));

    StopProxyMock stopA = new StopProxyMock("a", locationA);
    StopProxyMock stopB = new StopProxyMock("b", locationB);

    StopTimeProxyMock sta = new StopTimeProxyMock(1, stopA);
    StopTimeProxyMock stb = new StopTimeProxyMock(2, stopB);

    long serviceDateA = 1000;
    long serviceDateB = 2000;

    StopTimeInstanceProxy stiA = new StopTimeInstanceProxy(sta, serviceDateA);
    StopTimeInstanceProxy stiB = new StopTimeInstanceProxy(stb, serviceDateB);

    VehicleContinuationState vcsA = new VehicleContinuationState(stiA);
    VehicleContinuationState vcsB = new VehicleContinuationState(stiA);
    VehicleContinuationState vcsC = new VehicleContinuationState(stiB);

    assertEquals(vcsA, vcsB);
    assertFalse(vcsA.equals(vcsC));
  }

  private class StopProxyMock implements StopProxy {

    private String _id;
    private Point _location;

    public StopProxyMock(String id, Point location) {
      _id = id;
      _location = location;
    }

    public String getStopId() {
      return _id;
    }

    public double getStopLat() {
      // TODO Auto-generated method stub
      return 0;
    }

    public Point getStopLocation() {
      return _location;
    }

    public double getStopLon() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof StopProxyMock))
        return false;
      StopProxyMock other = (StopProxyMock) obj;
      return _id.equals(other._id);
    }

    @Override
    public int hashCode() {
      return _id.hashCode();
    }

  }

  private class StopTimeProxyMock implements StopTimeProxy {

    private Integer _id;
    private StopProxyMock _stop;

    public StopTimeProxyMock(Integer id, StopProxyMock stop) {
      _id = id;
      _stop = stop;
    }

    public int getArrivalTime() {
      // TODO Auto-generated method stub
      return 0;
    }

    public int getDepartureTime() {
      // TODO Auto-generated method stub
      return 0;
    }

    public Integer getId() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getRouteId() {
      // TODO Auto-generated method stub
      return null;
    }

    public int getSequence() {
      // TODO Auto-generated method stub
      return 0;
    }

    public StopProxy getStop() {
      return _stop;
    }

    public String getTripId() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof StopTimeProxyMock))
        return false;
      StopTimeProxyMock other = (StopTimeProxyMock) obj;
      return _id.equals(other._id);
    }

    @Override
    public int hashCode() {
      return _id.hashCode();
    }
  }
}

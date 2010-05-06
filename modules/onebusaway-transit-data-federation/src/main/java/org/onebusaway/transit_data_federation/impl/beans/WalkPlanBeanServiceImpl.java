package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkNode;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.beans.WalkPlanBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

import edu.washington.cs.rse.collections.adapter.AdapterLibrary;
import edu.washington.cs.rse.collections.adapter.IAdapter;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
class WalkPlanBeanServiceImpl implements WalkPlanBeanService {

  private static WalkNodePointAdapter _walkNodePointAdapter = new WalkNodePointAdapter();

  public WalkSegmentBean getWalkPlanAsBean(long startTime, long duration,
      WalkPlan walkPlan) {

    List<WalkNode> path = walkPlan.getPath();
    Iterable<CoordinatePoint> points = AdapterLibrary.adapt(path,
        _walkNodePointAdapter);
    EncodedPolylineBean polyline = PolylineEncoder.createEncodings(points);
    return new WalkSegmentBean(startTime, polyline, walkPlan.getDistance(),
        duration);
  }

  public WalkSegmentBean getStopsAsBean(long startTime, long duration,
      StopEntry stopFrom, StopEntry stopTo) {
    double[] lat = {stopFrom.getStopLat(), stopTo.getStopLat()};
    double[] lon = {stopFrom.getStopLon(), stopTo.getStopLon()};
    double distance = DistanceLibrary.distance(stopFrom.getStopLocation(),
        stopTo.getStopLocation());
    return new WalkSegmentBean(startTime, PolylineEncoder.createEncodings(lat,
        lon), distance, duration);
  }

  private static class WalkNodePointAdapter implements
      IAdapter<WalkNode, CoordinatePoint> {

    public CoordinatePoint adapt(WalkNode source) {
      ProjectedPoint location = source.getLocation();
      return new CoordinatePoint(location.getLat(), location.getLon());
    }
  }

}

package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkNode;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.beans.WalkPlanBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.springframework.stereotype.Component;

@Component
class WalkPlanBeanServiceImpl implements WalkPlanBeanService {

  public WalkSegmentBean getWalkPlanAsBean(long startTime, long duration,
      WalkPlan walkPlan) {

    List<WalkNode> path = walkPlan.getPath();
    
    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();
    for (WalkNode node : path) {
      ProjectedPoint location = node.getLocation();
      points.add(new CoordinatePoint(location.getLat(), location.getLon()));
    }
    
    EncodedPolylineBean polyline = PolylineEncoder.createEncodings(points);
    return new WalkSegmentBean(startTime, polyline, walkPlan.getDistance(),
        duration);
  }

  public WalkSegmentBean getStopsAsBean(long startTime, long duration,
      StopEntry stopFrom, StopEntry stopTo) {
    double[] lat = {stopFrom.getStopLat(), stopTo.getStopLat()};
    double[] lon = {stopFrom.getStopLon(), stopTo.getStopLon()};
    double distance = SphericalGeometryLibrary.distance(
        stopFrom.getStopLocation(), stopTo.getStopLocation());
    return new WalkSegmentBean(startTime, PolylineEncoder.createEncodings(lat,
        lon), distance, duration);
  }
}

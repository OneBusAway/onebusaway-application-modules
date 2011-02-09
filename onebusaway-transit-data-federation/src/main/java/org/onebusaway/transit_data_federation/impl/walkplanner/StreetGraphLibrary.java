package org.onebusaway.transit_data_federation.impl.walkplanner;

import org.onebusaway.geospatial.model.PointVector;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkEdgeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreetGraphLibrary {

  private static Logger _log = LoggerFactory.getLogger(StreetGraphLibrary.class);

  public static ProjectedPoint computeClosestPointOnEdge(WalkEdgeEntry edge,
      ProjectedPoint point) {

    WalkNodeEntry nodeFrom = edge.getNodeFrom();
    WalkNodeEntry nodeTo = edge.getNodeTo();

    ProjectedPoint pointFrom = nodeFrom.getLocation();
    ProjectedPoint pointTo = nodeTo.getLocation();

    ProjectedPoint pointOnEdge = projectPointOntoSegment(point, pointFrom,
        pointTo);

    double edgeLength = edge.getDistance();
    double distanceFromNode = pointFrom.distance(pointOnEdge);

    // If the point on the edge is beyond the endpoints of the edge
    if (distanceFromNode > edgeLength
        || pointTo.distance(pointOnEdge) > edgeLength) {

      double distanceToNodeFrom = point.distance(pointFrom);
      double distanceToNodeTo = point.distance(pointTo);

      if (distanceToNodeFrom < distanceToNodeTo)
        return pointFrom;
      else
        return pointTo;
    }

    return pointOnEdge;
  }

  private static ProjectedPoint projectPointOntoSegment(ProjectedPoint point,
      ProjectedPoint segmentStart, ProjectedPoint segmentEnd) {

    segmentStart = ProjectedPointFactory.ensureSrid(segmentStart,
        point.getSrid());
    segmentEnd = ProjectedPointFactory.ensureSrid(segmentEnd, point.getSrid());

    if (segmentStart.getX() == segmentEnd.getX()
        && segmentStart.getY() == segmentEnd.getY())
      return segmentStart;

    PointVector v = new PointVector(point.getX() - segmentStart.getX(),
        point.getY() - segmentStart.getY());
    PointVector line = new PointVector(segmentEnd.getX() - segmentStart.getX(),
        segmentEnd.getY() - segmentStart.getY());
    PointVector proj = line.getProjection(v);

    double x = segmentStart.getX() + proj.getX();
    double y = segmentStart.getY() + proj.getY();

    try {
      return ProjectedPointFactory.reverse(x, y, point.getSrid());
    } catch (Exception ex) {
      _log.warn("error in reverse point projection: point=" + point
          + " segmentStart=" + segmentStart + " segmentEnd=" + segmentEnd
          + " result=" + x + "," + y, ex);
      throw new IllegalStateException(ex);
    }
  }
}

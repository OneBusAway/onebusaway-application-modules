package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.services.NoPathException;

import com.vividsolutions.jts.geom.Point;

public class DirectDistanceToEndImpl implements TripStateScoringStrategy {

  private Point _endPoint;

  private double _transitVelocity;

  public DirectDistanceToEndImpl(Point endPoint, double transitVelocity) {
    _endPoint = endPoint;
    _transitVelocity = transitVelocity;
  }

  public double getMinScoreForTripState(TripState state) throws NoPathException {
    Point from = state.getLocation();
    double distance = UtilityLibrary.distance(from, _endPoint);
    return distance / _transitVelocity;
  }
}

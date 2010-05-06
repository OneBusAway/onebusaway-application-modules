package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.where.model.StopTimeInstance;

public abstract class VehicleState extends TripState {

  private final StopTimeInstance _sti;

  public VehicleState(long currentTime,
      StopTimeInstance sti) {
    super(currentTime);
    _sti = sti;
  }

  public StopTimeInstance getStopTimeInstance() {
    return _sti;
  }

  @Override
  public Point getLocation() {
    StopTime st = _sti.getStopTime();
    Stop stop = st.getStop();
    return stop.getLocation();
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    VehicleState vs = (VehicleState) obj;
    return _sti.equals(vs._sti);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _sti.hashCode();
  }
}

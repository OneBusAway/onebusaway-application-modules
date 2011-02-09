package org.onebusaway.transit_data.model.trips;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripsForBoundsQueryBean extends AbstractTripsQueryBean {

  private static final long serialVersionUID = 1L;

  private CoordinateBounds bounds;

  public CoordinateBounds getBounds() {
    return bounds;
  }

  public void setBounds(CoordinateBounds bounds) {
    this.bounds = bounds;
  }
}

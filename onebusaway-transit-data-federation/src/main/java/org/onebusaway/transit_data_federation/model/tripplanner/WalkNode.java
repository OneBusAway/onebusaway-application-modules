/**
 * 
 */
package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.model.ProjectedPoint;

import java.io.Serializable;

public class WalkNode implements Serializable {

  private static final long serialVersionUID = 1L;

  private final ProjectedPoint location;

  public WalkNode(ProjectedPoint location) {
    this.location = location;
  }

  public ProjectedPoint getLocation() {
    return location;
  }
}
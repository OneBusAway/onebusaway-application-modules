package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.Collections;
import java.util.Set;

import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.MutableEdgeNarrative;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.Vertex;

import com.vividsolutions.jts.geom.Geometry;

public class EdgeNarrativeImpl implements EdgeNarrative, MutableEdgeNarrative {

  private Vertex fromVertex;

  private Vertex toVertex;

  public EdgeNarrativeImpl(Vertex fromVertex, Vertex toVertex) {
    this.fromVertex = fromVertex;
    this.toVertex = toVertex;
  }

  /****
   * {@link MutableEdgeNarrative} Interface
   ****/

  @Override
  public void setFromVertex(Vertex fromVertex) {
    this.fromVertex = fromVertex;
  }

  @Override
  public void setToVertex(Vertex toVertex) {
    this.toVertex = toVertex;
  }

  /****
   * {@link EdgeNarrative} Interface
   ****/

  @Override
  public Vertex getFromVertex() {
    return fromVertex;
  }

  @Override
  public Vertex getToVertex() {
    return toVertex;
  }

  @Override
  public TraverseMode getMode() {
    return TraverseMode.TRANSIT;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Geometry getGeometry() {
    return null;
  }

  @Override
  public double getDistance() {
    return 0;
  }

  @Override
  public Trip getTrip() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRoundabout() {
    return false;
  }

  @Override
  public Set<String> getNotes() {
    return Collections.emptySet();
  }

  /****
   * {@link Object} Interface
   ****/

  public String toString() {
    return "EdgeNarrative(from=" + fromVertex + " to=" + toVertex + ")";
  }
}

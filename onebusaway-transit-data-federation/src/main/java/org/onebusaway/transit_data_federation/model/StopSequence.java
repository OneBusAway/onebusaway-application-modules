package org.onebusaway.transit_data_federation.model;

import java.util.List;

import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data_federation.services.StopSequencesService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

/**
 * A stop sequence is a unique sequence of stops visited by a transit trip. So
 * typically, multiple trips will often refer to the same stop sequence, but not
 * always.
 * 
 * @author bdferris
 * @see StopSequencesService
 */
public class StopSequence extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private Route route;

  private List<StopEntry> stops;

  private List<BlockTripEntry> trips;

  private int tripCount;

  private String directionId;

  private AgencyAndId shapeId;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public List<StopEntry> getStops() {
    return this.stops;
  }

  public void setStops(List<StopEntry> stops) {
    this.stops = stops;
  }

  public List<BlockTripEntry> getTrips() {
    return this.trips;
  }

  public void setTrips(List<BlockTripEntry> trips) {
    this.trips = trips;
  }

  public int getTripCount() {
    return tripCount;
  }

  public void setTripCount(int tripCount) {
    this.tripCount = tripCount;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  @Override
  public String toString() {
    return "StopSequence(id=" + this.id + " directionId=" + this.directionId
        + " trips=" + this.tripCount + ")";
  }
}

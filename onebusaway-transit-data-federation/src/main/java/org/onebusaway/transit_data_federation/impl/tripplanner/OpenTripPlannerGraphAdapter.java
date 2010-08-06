package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.opentripplanner.routing.core.Graph;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

public class OpenTripPlannerGraphAdapter implements TripPlannerGraph {

  public void setGraph(Graph graph) {
    
  }

  @PostConstruct
  public void setup() {

  }

  @Override
  public Iterable<StopEntry> getAllStops() {

    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<TripEntry> getAllTrips() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StopEntry getStopEntryForId(AgencyAndId id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<StopEntry> getStopsByLocation(CoordinateRectangle bounds) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TripEntry getTripEntryForId(AgencyAndId id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TripEntry> getTripsForBlockId(AgencyAndId blockId) {
    // TODO Auto-generated method stub
    return null;
  }
  
  

}

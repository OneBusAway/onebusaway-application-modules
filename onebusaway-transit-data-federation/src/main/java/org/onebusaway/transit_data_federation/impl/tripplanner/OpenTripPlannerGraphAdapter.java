package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.TransitStop;
import org.opentripplanner.routing.core.Vertex;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

public class OpenTripPlannerGraphAdapter implements TripPlannerGraph {

  private Graph _graph;

  private List<TransitStop> _stops = new ArrayList<TransitStop>();
  
  private Map<AgencyAndId,TransitStop> _stopsById = new HashMap<AgencyAndId, TransitStop>();

  public void setGraph(Graph graph) {
    _graph = graph;
  }

  @PostConstruct
  public void setup() {
    for (Vertex vertex : _graph.getVertices()) {
      if (vertex instanceof TransitStop) {
        TransitStop stop = (TransitStop) vertex;
        _stops.add(stop);
      }
    }
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
    TransitStop stop = _stopsById.get(id);
    
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

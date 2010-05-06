package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.model.AbstractGraph;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import edu.washington.cs.rse.collections.tuple.Pair;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TripPlannerGraphImpl implements Serializable, TripPlannerGraph {

  private static final long serialVersionUID = 1L;

  private Map<String, TripEntryImpl> _tripEntries = new HashMap<String, TripEntryImpl>();

  private Map<String, StopEntryImpl> _stopEntries = new HashMap<String, StopEntryImpl>();

  private Map<Pair<String>, Integer> _minTransitTimes = new HashMap<Pair<String>, Integer>();

  private transient RTree _stopLocationTree = null;

  private List<String> _stops = new ArrayList<String>();

  public TripPlannerGraphImpl() {

  }

  public void initialize() {
    if (_stopLocationTree == null) {
      System.out.println("initializing trip planner graph...");

      _stopLocationTree = new RTree();
      _stopLocationTree.init(new Properties());

      for (int i = 0; i < _stops.size(); i++) {
        String stopId = _stops.get(i);
        StopEntryImpl entry = getStopEntryByStopId(stopId);
        StopProxy proxy = entry.getProxy();
        Point p = proxy.getStopLocation();
        float x = (float) p.getX();
        float y = (float) p.getY();
        Rectangle r = new Rectangle(x, y, x, y);
        _stopLocationTree.add(r, i);
      }
    }
  }

  public void putTripEntry(String tripId, TripEntryImpl tripEntry) {
    _tripEntries.put(tripId, tripEntry);
  }

  public void putStopEntry(String stopId, StopEntryImpl stopEntry) {
    StopEntryImpl existing = _stopEntries.put(stopId, stopEntry);
    if (existing != null)
      throw new IllegalStateException();
    _stops.add(stopId);
  }

  public Collection<String> getTripIds() {
    return _tripEntries.keySet();
  }

  public TripEntryImpl getTripEntryByTripId(String id) {
    return _tripEntries.get(id);
  }

  public StopEntryImpl getStopEntryByStopId(String id) {
    return _stopEntries.get(id);
  }

  public Set<String> getStopIds() {
    return _stopEntries.keySet();
  }

  public List<String> getStopsByLocation(Geometry boundary) {

    Rectangle r = AbstractGraph.getLocationAsRectangle(boundary);
    StopRTreeVisitor go = new StopRTreeVisitor();
    _stopLocationTree.intersects(r, go);
    return go.getStops();
  }

  public void setMinTransitTime(String fromStopId, String toStopId, int time) {
    Pair<String> pair = Pair.createPair(fromStopId, toStopId);
    Integer current = _minTransitTimes.get(pair);
    if (current == null || time < current)
      _minTransitTimes.put(pair, time);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.onebusaway.tripplanner.impl.TripPlannerGraph#getMinTransitTime(java
   * .lang.String, java.lang.String)
   */
  public int getMinTransitTime(String fromStopId, String toStopId) {
    Pair<String> pair = Pair.createPair(fromStopId, toStopId);
    Integer time = _minTransitTimes.get(pair);
    if (time == null)
      return -1;
    return time;
  }

  private class StopRTreeVisitor implements IntProcedure {

    private List<String> _nearbyStops = new ArrayList<String>();

    public List<String> getStops() {
      return _nearbyStops;
    }

    public boolean execute(int id) {
      _nearbyStops.add(_stops.get(id));
      return true;
    }
  }

}

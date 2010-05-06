package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.collections.tuple.Pair;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TripPlannerGraph implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, TripEntry> _tripEntries = new HashMap<String, TripEntry>();

  private Map<String, StopEntry> _stopEntries = new HashMap<String, StopEntry>();

  private Map<Pair<String>, Integer> _minTransitTimes = new HashMap<Pair<String>, Integer>();

  private transient RTree _stopLocationTree = null;

  private List<Stop> _stops = new ArrayList<Stop>();

  public TripPlannerGraph() {

  }

  public void initialize() {
    if (_stopLocationTree == null) {

      _stopLocationTree = new RTree();
      _stopLocationTree.init(new Properties());

      for (int i = 0; i < _stops.size(); i++) {
        Stop stop = _stops.get(i);
        Point p = stop.getLocation();
        float x = (float) p.getX();
        float y = (float) p.getY();
        Rectangle r = new Rectangle(x, y, x, y);
        _stopLocationTree.add(r, i);
      }
    }
  }

  public void putTripEntry(String tripId, TripEntry tripEntry) {
    _tripEntries.put(tripId, tripEntry);
  }

  public void putStopEntry(Stop stop, StopEntry stopEntry) {
    _stopEntries.put(stop.getId(), stopEntry);
    _stops.add(stop);
  }

  public TripEntry getTripEntryByTripId(String id) {
    return _tripEntries.get(id);
  }

  public StopEntry getStopEntryByStopId(String id) {
    return _stopEntries.get(id);
  }

  public Set<String> getStopIds() {
    return _stopEntries.keySet();
  }

  public List<Stop> getStopsByLocation(Geometry boundary) {

    Rectangle r = AbstractGraph.getLocationAsRectangle(boundary);
    Go go = new Go();
    _stopLocationTree.intersects(r, go);
    return go.getStops();
  }

  public void setMinTransitTime(String fromStopId, String toStopId, int time) {
    Pair<String> pair = Pair.createPair(fromStopId, toStopId);
    Integer current = _minTransitTimes.get(pair);
    if (current == null || time < current)
      _minTransitTimes.put(pair, time);
  }

  public int getMinTransitTime(String fromStopId, String toStopId) {
    Pair<String> pair = Pair.createPair(fromStopId, toStopId);
    Integer time = _minTransitTimes.get(pair);
    if (time == null)
      return -1;
    return time;
  }

  private class Go implements IntProcedure {

    private List<Stop> _nearbyStops = new ArrayList<Stop>();

    public List<Stop> getStops() {
      return _nearbyStops;
    }

    public boolean execute(int id) {
      _nearbyStops.add(_stops.get(id));
      return true;
    }
  }

}

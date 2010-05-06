package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.serialization.EntryIdAndCallback;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import edu.washington.cs.rse.collections.adapter.IAdapter;
import edu.washington.cs.rse.collections.adapter.IterableAdapter;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TripPlannerGraphImpl implements Serializable, TripPlannerGraph {

  private static final long serialVersionUID = 1L;

  private static final TripEntryAdapter _tripEntryAdapter = new TripEntryAdapter();

  private static final StopEntryAdapter _stopEntryAdapter = new StopEntryAdapter();

  private transient static ReadHelper _helper;

  private List<StopEntryImpl> _stops = new ArrayList<StopEntryImpl>();

  private List<TripEntryImpl> _trips = new ArrayList<TripEntryImpl>();

  private transient RTree _stopLocationTree = null;

  private transient Map<AgencyAndId, TripEntryImpl> _tripEntriesById = new HashMap<AgencyAndId, TripEntryImpl>();

  private transient Map<AgencyAndId, StopEntryImpl> _stopEntriesById = new HashMap<AgencyAndId, StopEntryImpl>();

  private transient Map<AgencyAndId, List<TripEntry>> _tripsByBlockId = new HashMap<AgencyAndId, List<TripEntry>>();

  public TripPlannerGraphImpl() {

  }

  public void initialize() {
    if (_stopLocationTree == null) {
      System.out.println("initializing trip planner graph...");

      _stopLocationTree = new RTree();
      _stopLocationTree.init(new Properties());

      for (int i = 0; i < _stops.size(); i++) {
        StopEntry stop = _stops.get(i);
        float x = (float) stop.getStopLon();
        float y = (float) stop.getStopLat();
        Rectangle r = new Rectangle(x, y, x, y);
        _stopLocationTree.add(r, i);
      }

      System.out.println("  stops=" + _stops.size());
      System.out.println("  trips= " + _trips.size());
    }

    if (_tripEntriesById == null) {
      _tripEntriesById = new HashMap<AgencyAndId, TripEntryImpl>();
      for (TripEntryImpl entry : _trips)
        _tripEntriesById.put(entry.getId(), entry);
    }

    if (_stopEntriesById == null) {
      _stopEntriesById = new HashMap<AgencyAndId, StopEntryImpl>();
      for (StopEntryImpl entry : _stops)
        _stopEntriesById.put(entry.getId(), entry);
    }

    if (_tripsByBlockId == null) {
      _tripsByBlockId = new HashMap<AgencyAndId, List<TripEntry>>();
      for (TripEntryImpl entry : _trips) {
        AgencyAndId blockId = entry.getBlockId();
        if (blockId != null && _tripsByBlockId.get(blockId) == null) {
          while (entry.getPrevTrip() != null)
            entry = entry.getPrevTrip();
          ArrayList<TripEntry> tripsForBlock = new ArrayList<TripEntry>();
          while (entry != null) {
            tripsForBlock.add(entry);
            entry = entry.getNextTrip();
          }
          tripsForBlock.trimToSize();
          _tripsByBlockId.put(blockId, tripsForBlock);
        }
      }
    }
  }

  public void putStopEntry(StopEntryImpl stopEntry) {
    _stops.add(stopEntry);
  }

  public List<StopEntryImpl> getStops() {
    return _stops;
  }

  public void putTripEntry(TripEntryImpl tripEntry) {
    _trips.add(tripEntry);
  }

  public List<TripEntryImpl> getTrips() {
    return _trips;
  }

  /****
   * {@link TripPlannerGraph} Interface
   ****/

  @Override
  public Iterable<StopEntry> getAllStops() {
    return new IterableAdapter<StopEntryImpl, StopEntry>(_stops,
        _stopEntryAdapter);
  }

  @Override
  public Iterable<TripEntry> getAllTrips() {
    return new IterableAdapter<TripEntryImpl, TripEntry>(_trips,
        _tripEntryAdapter);
  }

  @Override
  public StopEntry getStopEntryForId(AgencyAndId id) {
    return _stopEntriesById.get(id);
  }

  @Override
  public TripEntry getTripEntryForId(AgencyAndId id) {
    return _tripEntriesById.get(id);
  }

  @Override
  public List<TripEntry> getTripsForBlockId(AgencyAndId blockId) {
    return _tripsByBlockId.get(blockId);
  }

  @Override
  public List<StopEntry> getStopsByLocation(CoordinateRectangle bounds) {
    Rectangle r = new Rectangle((float) bounds.getMinLon(),
        (float) bounds.getMinLat(), (float) bounds.getMaxLon(),
        (float) bounds.getMaxLat());
    StopRTreeVisitor go = new StopRTreeVisitor();
    _stopLocationTree.intersects(r, go);
    return go.getStops();
  }

  private class StopRTreeVisitor implements IntProcedure {

    private List<StopEntry> _nearbyStops = new ArrayList<StopEntry>();

    public List<StopEntry> getStops() {
      return _nearbyStops;
    }

    public boolean execute(int id) {
      _nearbyStops.add(_stops.get(id));
      return true;
    }
  }

  /*****************************************************************************
   * Serialization Support
   ****************************************************************************/

  public static void handleStopEntryRead(StopEntryImpl stopEntryImpl) {
    _helper.handleStopEntryRead(stopEntryImpl);
  }

  public static void handleTripEntryRead(TripEntryImpl tripEntryImpl) {
    _helper.handleTripEntryRead(tripEntryImpl);
  }

  public static void addStopEntryCallback(AgencyAndId stopEntry,
      EntryCallback<StopEntryImpl> entry) {
    _helper.addStopEntryCallback(stopEntry, entry);
  }

  public static void addTripEntryCallback(AgencyAndId tripEntry,
      EntryCallback<TripEntryImpl> entry) {
    _helper.addTripEntryCallback(tripEntry, entry);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    _helper = new ReadHelper();
    in.defaultReadObject();
    _helper.flush();
    _helper = null;

    initialize();

    // Do a GC run, since the graph-reading process requires a lot of data to be
    // loaded
    System.gc();
    System.gc();
  }

  private static class ReadHelper {

    private Map<AgencyAndId, StopEntryImpl> _stops = new HashMap<AgencyAndId, StopEntryImpl>();

    private Map<AgencyAndId, TripEntryImpl> _trips = new HashMap<AgencyAndId, TripEntryImpl>();

    private List<EntryIdAndCallback<AgencyAndId, StopEntryImpl>> _stopCallbacks = new ArrayList<EntryIdAndCallback<AgencyAndId, StopEntryImpl>>();
    private List<EntryIdAndCallback<AgencyAndId, TripEntryImpl>> _tripCallbacks = new ArrayList<EntryIdAndCallback<AgencyAndId, TripEntryImpl>>();

    public void handleStopEntryRead(StopEntryImpl stopEntryImpl) {
      _stops.put(stopEntryImpl.getId(), stopEntryImpl);
    }

    public void handleTripEntryRead(TripEntryImpl tripEntryImpl) {
      _trips.put(tripEntryImpl.getId(), tripEntryImpl);
    }

    public void addStopEntryCallback(AgencyAndId stopEntryId,
        EntryCallback<StopEntryImpl> callback) {
      _stopCallbacks.add(new EntryIdAndCallback<AgencyAndId, StopEntryImpl>(
          stopEntryId, callback));
    }

    public void addTripEntryCallback(AgencyAndId tripEntryId,
        EntryCallback<TripEntryImpl> callback) {
      _tripCallbacks.add(new EntryIdAndCallback<AgencyAndId, TripEntryImpl>(
          tripEntryId, callback));
    }

    public void flush() {

      for (EntryIdAndCallback<AgencyAndId, StopEntryImpl> ci : _stopCallbacks) {
        StopEntryImpl entry = _stops.get(ci.getId());
        if (entry == null)
          throw new IllegalStateException("no such stop entry: " + ci.getId());
        ci.getCallback().handle(entry);
      }

      for (EntryIdAndCallback<AgencyAndId, TripEntryImpl> ci : _tripCallbacks) {
        TripEntryImpl entry = _trips.get(ci.getId());
        if (entry == null)
          throw new IllegalStateException("no such trip entry: " + ci.getId());
        ci.getCallback().handle(entry);
      }

      _stopCallbacks.clear();
      _tripCallbacks.clear();

      _stopCallbacks = null;
      _tripCallbacks = null;

      _stops.clear();
      _trips.clear();

      _stops = null;
      _trips = null;
    }
  }

  private static class TripEntryAdapter implements
      IAdapter<TripEntryImpl, TripEntry> {

    @Override
    public TripEntry adapt(TripEntryImpl source) {
      return source;
    }
  }

  private static class StopEntryAdapter implements
      IAdapter<StopEntryImpl, StopEntry> {

    @Override
    public StopEntry adapt(StopEntryImpl source) {
      return source;
    }
  }

}

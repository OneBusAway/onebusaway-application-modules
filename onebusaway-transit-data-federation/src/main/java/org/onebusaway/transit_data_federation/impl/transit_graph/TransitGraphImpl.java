package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.adapter.IAdapter;
import org.onebusaway.collections.adapter.IterableAdapter;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.serialization.EntryIdAndCallback;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

public class TransitGraphImpl implements Serializable, TripPlannerGraph {

  private static final long serialVersionUID = 1L;

  private static final TripEntryAdapter _tripEntryAdapter = new TripEntryAdapter();

  private static final BlockEntryAdapter _blockEntryAdapter = new BlockEntryAdapter();

  private static final StopEntryAdapter _stopEntryAdapter = new StopEntryAdapter();

  private transient static ReadHelper _helper;

  private List<StopEntryImpl> _stops = new ArrayList<StopEntryImpl>();

  private List<TripEntryImpl> _trips = new ArrayList<TripEntryImpl>();

  private List<BlockEntryImpl> _blocks = new ArrayList<BlockEntryImpl>();

  private transient STRtree _stopLocationTree = null;

  private transient Map<AgencyAndId, StopEntryImpl> _stopEntriesById = new HashMap<AgencyAndId, StopEntryImpl>();

  private transient Map<AgencyAndId, TripEntryImpl> _tripEntriesById = new HashMap<AgencyAndId, TripEntryImpl>();

  private transient Map<AgencyAndId, BlockEntryImpl> _blockEntriesById = new HashMap<AgencyAndId, BlockEntryImpl>();

  public TransitGraphImpl() {

  }

  public void initialize() {
    if (_stopLocationTree == null) {
      System.out.println("initializing trip planner graph...");

      _stopLocationTree = new STRtree(_stops.size());

      for (int i = 0; i < _stops.size(); i++) {
        StopEntry stop = _stops.get(i);
        double x = stop.getStopLon();
        double y = stop.getStopLat();
        Envelope r = new Envelope(x, x, y, y);
        _stopLocationTree.insert(r, stop);
      }

      _stopLocationTree.build();

      System.out.println("  stops=" + _stops.size());
      System.out.println("  trips= " + _trips.size());
    }

    if (_tripEntriesById == null || _tripEntriesById.size() < _trips.size()) {
      refreshTripMapping();
    }

    if (_blockEntriesById == null || _blockEntriesById.size() < _blocks.size()) {
      refreshBlockMapping();
    }

    if (_stopEntriesById == null || _stopEntriesById.size() < _stops.size())
      refreshStopMapping();
  }

  public void initializeFromExistinGraph(TransitGraphImpl graph) {
    _stops.addAll(graph._stops);
    _trips.addAll(graph._trips);
    _blocks.addAll(graph._blocks);
    initialize();
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

  public void putBlockEntry(BlockEntryImpl blockEntry) {
    _blocks.add(blockEntry);
  }

  public void refreshTripMapping() {
    _tripEntriesById = new HashMap<AgencyAndId, TripEntryImpl>();
    for (TripEntryImpl entry : _trips)
      _tripEntriesById.put(entry.getId(), entry);
  }

  public void refreshBlockMapping() {
    _blockEntriesById = new HashMap<AgencyAndId, BlockEntryImpl>();
    for (BlockEntryImpl entry : _blocks)
      _blockEntriesById.put(entry.getId(), entry);
  }

  public void refreshStopMapping() {
    _stopEntriesById = new HashMap<AgencyAndId, StopEntryImpl>();
    for (StopEntryImpl entry : _stops)
      _stopEntriesById.put(entry.getId(), entry);
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
  public Iterable<BlockEntry> getAllBlocks() {
    return new IterableAdapter<BlockEntryImpl, BlockEntry>(_blocks,
        _blockEntryAdapter);
  }

  @Override
  public StopEntryImpl getStopEntryForId(AgencyAndId id) {
    return _stopEntriesById.get(id);
  }

  @Override
  public TripEntryImpl getTripEntryForId(AgencyAndId id) {
    return _tripEntriesById.get(id);
  }

  @Override
  public BlockEntry getBlockEntryForId(AgencyAndId blockId) {
    return _blockEntriesById.get(blockId);
  }

  @Override
  public List<StopEntry> getStopsByLocation(CoordinateBounds bounds) {
    Envelope r = new Envelope(bounds.getMinLon(), bounds.getMaxLon(),
        bounds.getMinLat(), bounds.getMaxLat());
    StopRTreeVisitor go = new StopRTreeVisitor();
    _stopLocationTree.query(r, go);
    return go.getStops();
  }

  private class StopRTreeVisitor implements ItemVisitor {

    private List<StopEntry> _nearbyStops = new ArrayList<StopEntry>();

    public List<StopEntry> getStops() {
      return _nearbyStops;
    }

    @Override
    public void visitItem(Object obj) {
      _nearbyStops.add((StopEntry) obj);
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

  private static class BlockEntryAdapter implements
      IAdapter<BlockEntryImpl, BlockEntry> {

    @Override
    public BlockEntry adapt(BlockEntryImpl source) {
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

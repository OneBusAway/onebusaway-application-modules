/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkEdgeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WalkNodeEntryImpl implements Serializable, WalkNodeEntry {

  private static final long serialVersionUID = 1L;

  private int _id;

  private ProjectedPoint _location;

  private List<WalkEdgeEntry> _edges = new ArrayList<WalkEdgeEntry>();

  public WalkNodeEntryImpl(int id, ProjectedPoint location) {
    _id = id;
    _location = location;
  }

  public int getId() {
    return _id;
  }

  public ProjectedPoint getLocation() {
    return _location;
  }

  public boolean hasEdges() {
    return !_edges.isEmpty();
  }

  public Iterable<WalkEdgeEntry> getEdges() {
    return _edges;
  }

  public void addEdge(WalkNodeEntry entry, double distance) {
    for (WalkEdgeEntry edge : _edges) {
      if (edge.getNodeTo().equals(entry))
        return;
    }
    _edges.add(new WalkEdgeEntryImpl(this, entry, distance));
  }

  @Override
  public int hashCode() {
    return _id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WalkNodeEntryImpl other = (WalkNodeEntryImpl) obj;
    return _id == other._id;
  }

  @Override
  public String toString() {
    return Integer.toString(_id) + " " + _location.getLat() + " " + _location.getLon();
  }

  /*****************************************************************************
   * Serialization Support
   ****************************************************************************/

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(_id);
    out.writeObject(_location);

    int[] ids = new int[_edges.size()];
    double[] distances = new double[_edges.size()];
    for (int i = 0; i < ids.length; i++) {
      WalkEdgeEntry entry = _edges.get(i);
      ids[i] = entry.getNodeTo().getId();
      distances[i] = entry.getDistance();
    }
    out.writeObject(ids);
    out.writeObject(distances);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {

    _id = in.readInt();
    _location = (ProjectedPoint) in.readObject();

    int[] ids = (int[]) in.readObject();
    double[] distances = (double[]) in.readObject();
    _edges = new ArrayList<WalkEdgeEntry>(ids.length);

    for (int i = 0; i < ids.length; i++) {
      final double distance = distances[i];
      WalkPlannerGraphImpl.addWalkNodeEntryCallback(ids[i],
          new EntryCallback<WalkNodeEntry>() {
            public void handle(WalkNodeEntry entry) {
              _edges.add(new WalkEdgeEntryImpl(WalkNodeEntryImpl.this, entry,
                  distance));
            }
          });
    }

    WalkPlannerGraphImpl.handleWalkNodeEntryRead(this);
  }
}
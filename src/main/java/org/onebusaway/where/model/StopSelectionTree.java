/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.model;

import org.onebusaway.gtdf.model.Stop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopSelectionTree implements Serializable {

  private static final long serialVersionUID = 1L;

  private Stop _stop = null;

  private Map<SelectionName, StopSelectionTree> _branches = new LinkedHashMap<SelectionName, StopSelectionTree>();

  public void setStop(Stop stop) {
    if (_stop != null && !_stop.equals(stop))
      throw new IllegalStateException("Stop bean already set!");
    _stop = stop;
  }

  public Set<SelectionName> getNames() {
    return _branches.keySet();
  }

  public boolean hasStop() {
    return _stop != null;
  }

  public Stop getStop() {
    return _stop;
  }

  public StopSelectionTree getSubTree(SelectionName name) {
    StopSelectionTree tree = _branches.get(name);
    if (tree == null) {
      tree = new StopSelectionTree();
      _branches.put(name, tree);
    }
    return tree;
  }

  public void moveSubTreeToBack(SelectionName name) {
    if (_branches.containsKey(name)) {
      StopSelectionTree branch = _branches.remove(name);
      _branches.put(name, branch);
    }
  }

  public List<Stop> getAllStops() {
    List<Stop> stops = new ArrayList<Stop>();
    getAllStopsRecursive(stops);
    return stops;
  }

  @Override
  public String toString() {
    return toStringRecursive("");
  }

  private void getAllStopsRecursive(List<Stop> stops) {
    if (_stop != null)
      stops.add(_stop);
    for (StopSelectionTree tree : _branches.values())
      tree.getAllStopsRecursive(stops);
  }

  private String toStringRecursive(String prefix) {
    if (hasStop())
      return prefix + getStop().getName();
    StringBuilder b = new StringBuilder();

    for (SelectionName name : getNames()) {
      if (b.length() > 0)
        b.append('\n');
      b.append(prefix).append(name.getName()).append('\n');
      b.append(getSubTree(name).toStringRecursive(prefix + "  "));
    }
    return b.toString();
  }
}
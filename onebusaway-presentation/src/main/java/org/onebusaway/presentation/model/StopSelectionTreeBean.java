/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.presentation.model;

import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.StopBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopSelectionTreeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private StopBean _stop = null;

  private Map<NameBean, StopSelectionTreeBean> _branches = new LinkedHashMap<NameBean, StopSelectionTreeBean>();

  public void setStop(StopBean stop) {
    if (_stop != null && !_stop.equals(stop))
      throw new IllegalStateException("Stop bean already set!");
    _stop = stop;
  }

  public Set<NameBean> getNames() {
    return _branches.keySet();
  }

  public boolean hasStop() {
    return _stop != null;
  }

  public StopBean getStop() {
    return _stop;
  }

  public StopSelectionTreeBean getSubTree(NameBean name) {
    StopSelectionTreeBean tree = _branches.get(name);
    if (tree == null) {
      tree = new StopSelectionTreeBean();
      _branches.put(name, tree);
    }
    return tree;
  }

  public void moveSubTreeToBack(NameBean name) {
    if (_branches.containsKey(name)) {
      StopSelectionTreeBean branch = _branches.remove(name);
      _branches.put(name, branch);
    }
  }

  public List<StopBean> getAllStops() {
    List<StopBean> stops = new ArrayList<StopBean>();
    getAllStopsRecursive(stops);
    return stops;
  }

  @Override
  public String toString() {
    return toStringRecursive("");
  }

  private void getAllStopsRecursive(List<StopBean> stops) {
    if (_stop != null)
      stops.add(_stop);
    for (StopSelectionTreeBean tree : _branches.values())
      tree.getAllStopsRecursive(stops);
  }

  private String toStringRecursive(String prefix) {
    if (hasStop())
      return prefix + getStop().getName();
    StringBuilder b = new StringBuilder();

    for (NameBean name : getNames()) {
      if (b.length() > 0)
        b.append('\n');
      b.append(prefix).append(name.getName()).append('\n');
      b.append(getSubTree(name).toStringRecursive(prefix + "  "));
    }
    return b.toString();
  }
}
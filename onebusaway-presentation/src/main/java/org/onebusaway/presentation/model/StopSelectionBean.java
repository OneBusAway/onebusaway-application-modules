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

public class StopSelectionBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<NameBean> _selected = new ArrayList<NameBean>();

  private Map<NameBean, StopBean> _names = new LinkedHashMap<NameBean, StopBean>();

  private List<StopBean> _stops = new ArrayList<StopBean>();

  /**
   * The set of names in the {@link StopSelectionTreeBean} that have been
   * selected thus far to get us to this point
   */

  public List<NameBean> getSelected() {
    return _selected;
  }

  public void addSelected(NameBean name) {
    _selected.add(name);
  }

  /**
   * The set of name branches at our current position in the
   * {@link StopSelectionTreeBean}
   */
  public Set<NameBean> getNames() {
    return _names.keySet();
  }

  public boolean hasStop(NameBean name) {
    return _names.get(name) != null;
  }

  public StopBean getStop(NameBean name) {
    return _names.get(name);
  }

  public void addName(NameBean name) {
    _names.put(name, null);
  }

  public void addNameWithStop(NameBean name, StopBean stop) {
    _names.put(name, stop);
  }

  public boolean hasStop() {
    return _stops.size() == 1;
  }

  public StopBean getStop() {
    return _stops.get(0);
  }

  public void setStop(StopBean stop) {
    _stops.add(stop);
  }

  public void addStop(StopBean stop) {
    _stops.add(stop);
  }

  /**
   * The set of stops at our current position in the
   * {@link StopSelectionTreeBean}
   * 
   * @return
   */
  public List<StopBean> getStops() {
    return _stops;
  }
  public String getType() {
    if (getNames() != null && !getNames().isEmpty()) {
      return getNames().iterator().next().getType();
    }
    return "unset";
  }
}
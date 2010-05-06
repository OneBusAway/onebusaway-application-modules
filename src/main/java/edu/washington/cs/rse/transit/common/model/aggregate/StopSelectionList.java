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
package edu.washington.cs.rse.transit.common.model.aggregate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.washington.cs.rse.transit.common.model.StopLocation;

public class StopSelectionList {

    private static final long serialVersionUID = 1L;

    private List<SelectionName> _selected = new ArrayList<SelectionName>();

    private Map<SelectionName, StopLocation> _names = new LinkedHashMap<SelectionName, StopLocation>();

    private StopLocation _stop = null;

    public List<SelectionName> getSelected() {
        return _selected;
    }

    public void addSelected(SelectionName name) {
        _selected.add(name);
    }

    public Set<SelectionName> getNames() {
        return _names.keySet();
    }

    public boolean hasStop(SelectionName name) {
        return _names.get(name) != null;
    }

    public StopLocation getStop(SelectionName name) {
        return _names.get(name);
    }

    public void addName(SelectionName name) {
        _names.put(name, null);
    }

    public void addNameWithStop(SelectionName name, StopLocation stop) {
        _names.put(name, stop);
    }

    public boolean hasStop() {
        return _stop != null;
    }

    public StopLocation getStop() {
        return _stop;
    }

    public void setStop(StopLocation stop) {
        _stop = stop;
    }
}
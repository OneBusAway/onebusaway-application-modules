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
package edu.washington.cs.rse.transit.web.oba.common.client.model;

import java.util.ArrayList;
import java.util.List;

public class StopAreaBean extends ApplicationBean {

    private static final long serialVersionUID = 1L;

    private StopBean _stop;

    private List<StopBean> _nearbyStops = new ArrayList<StopBean>();

    public StopAreaBean() {

    }

    public StopAreaBean(StopAreaBean bean) {
        _stop = bean.getStop();
        _nearbyStops.addAll(bean.getNearbyStops());
    }

    public StopBean getStop() {
        return _stop;
    }

    public void setStopAndIntersectionBean(StopBean stop) {
        _stop = stop;
    }

    public void addNearbyStop(StopBean stop) {
        _nearbyStops.add(stop);
    }

    public List<StopBean> getNearbyStops() {
        return _nearbyStops;
    }
}

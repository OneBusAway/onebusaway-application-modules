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
package edu.washington.cs.rse.transit.phone.actions.schedule;

import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithArrivalsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchStopServiceException;

public class PredictedScheduleByStopAction extends AbstractPredictedScheduleAction {

    private static final long serialVersionUID = 1L;

    private int _stopId;

    public void setStopId(int stopId) {
        _stopId = stopId;
    }

    public int getStopId() {
        return _stopId;
    }

    public String execute() throws Exception {

        try {
            StopWithArrivalsBean bean = _obaService.getArrivalsByStopId(_stopId);
            _predictions = bean.getPredictedArrivals();
            _bookmarkService.setLastLocationByStop(_userId, _stopId);
        } catch (NoSuchStopServiceException ex) {
            return INPUT;
        }

        return SUCCESS;
    }
}

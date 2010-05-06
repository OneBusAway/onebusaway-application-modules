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
package edu.washington.cs.rse.transit.web.oba.standard.client.pages.constraints;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;

import edu.washington.cs.rse.transit.web.oba.common.client.OneBusAwayCommon;
import edu.washington.cs.rse.transit.web.oba.common.client.OneBusAwayMessages;
import edu.washington.cs.rse.transit.web.oba.common.client.TargetSupport;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.OneBusAwayServiceAsync;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.IndexPageConstants;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.SearchWrapper;

public abstract class AbstractConstraint extends TargetSupport implements StopSelectionConstraint, IndexPageConstants {

    protected static OneBusAwayMessages _msgs = OneBusAwayCommon.MESSAGES;

    protected static OneBusAwayServiceAsync _service = OneBusAwayServiceAsync.SERVICE;

    protected Panel _resultsPanel;

    protected MapWidget _map;

    protected AsyncCallback<StopsBean> _stopsHandler;

    protected SearchWrapper _wrapper;

    /***************************************************************************
     * {@link StopSelectionConstraint} Interface
     **************************************************************************/

    public void setResultsPanel(Panel resultsPanel) {
        _resultsPanel = resultsPanel;
    }

    public void setMap(MapWidget map) {
        _map = map;
    }

    public void setStopsHandler(AsyncCallback<StopsBean> stopsHandler) {
        _stopsHandler = stopsHandler;
    }

    public void setSearchWrapper(SearchWrapper wrapper) {
        _wrapper = wrapper;
    }
}

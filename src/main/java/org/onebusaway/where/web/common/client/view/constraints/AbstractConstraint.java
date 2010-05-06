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
package org.onebusaway.where.web.common.client.view.constraints;

import org.onebusaway.where.web.common.client.WhereLibrary;
import org.onebusaway.where.web.common.client.WhereMessages;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.rpc.WhereServiceAsync;
import org.onebusaway.where.web.common.client.view.StopFinderInterface;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;

public abstract class AbstractConstraint implements StopSelectionConstraint {

  protected static WhereMessages _msgs = WhereLibrary.MESSAGES;

  protected static WhereServiceAsync _service = WhereServiceAsync.SERVICE;

  protected Panel _resultsPanel;

  protected MapWidget _map;

  protected AsyncCallback<StopsBean> _stopsHandler;

  protected StopFinderInterface _stopFinder;

  /***************************************************************************
   * {@link StopSelectionConstraint} Interface
   **************************************************************************/

  public void setResultsPanel(Panel resultsPanel) {
    _resultsPanel = resultsPanel;
  }

  public void setMap(MapWidget map) {
    _map = map;
  }

  public void setStopFinderInterface(StopFinderInterface wrapper) {
    _stopFinder = wrapper;
    _stopsHandler = wrapper.getStopsHandler();
  }
}

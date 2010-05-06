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
/**
 * 
 */
package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderInterface;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.ui.Panel;

public interface StopSelectionConstraint {

  public void setResultsPanel(Panel resultsPanel);

  public void setMap(MapWidget map);

  public void setStopFinderInterface(StopFinderInterface stopFinderInterface);

  public void update(Context context);
}
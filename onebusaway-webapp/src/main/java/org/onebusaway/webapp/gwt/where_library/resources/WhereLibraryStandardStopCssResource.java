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
package org.onebusaway.webapp.gwt.where_library.resources;

import com.google.gwt.resources.client.CssResource;

public interface WhereLibraryStandardStopCssResource extends CssResource {

  public String arrivalsStopInfo();

  public String arrivalsStopAddress();

  public String arrivalsStopNumber();

  public String arrivalsTable();

  public String arrivalsHeader();

  public String arrivalsRouteColumn();

  public String arrivalsDestinationColumn();

  public String arrivalsStatusColumn();

  public String arrivalsRow();

  public String arrivalsRouteEntry();

  public String arrivalsRouteLongNameEntry();
  
  public String arrivalsDestinationEntry();

  public String arrivalsTimePanel();

  public String arrivalsTimeEntry();

  public String arrivalsLabelEntry();

  public String arrivalsStatusEntry();

  public String arrivalStatusCancelled();

  public String arrivalStatusNow();

  public String arrivalsFilterPanel();

  public String arrivalsStatusUpdates();

  public String arrivalsRefreshButton();

  public String arrivalsNearbyStops();

  public String arrivalsSearchForStops();
}

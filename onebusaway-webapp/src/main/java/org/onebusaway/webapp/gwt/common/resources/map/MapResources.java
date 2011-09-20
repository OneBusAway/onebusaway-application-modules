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
package org.onebusaway.webapp.gwt.common.resources.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

public interface MapResources extends ClientBundle {

  public static MapResources INSTANCE = GWT.create(MapResources.class);

  @Source("Marker.png")
  public DataResource getImageMarker();

  /****
   * Map Icons - Bus
   ****/

  @Source("MapIcon-Bus-10.png")
  public ImageResource getIconBus10();

  @Source("MapIcon-Bus-14.png")
  public ImageResource getIconBus14();

  @Source("MapIcon-Bus-17.png")
  public ImageResource getIconBus17();

  @Source("MapIcon-Bus-17-North.png")
  public ImageResource getIconBus17North();

  @Source("MapIcon-Bus-17-South.png")
  public ImageResource getIconBus17South();

  @Source("MapIcon-Bus-17-East.png")
  public ImageResource getIconBus17East();

  @Source("MapIcon-Bus-17-West.png")
  public ImageResource getIconBus17West();

  @Source("MapIcon-Bus-17-NorthWest.png")
  public ImageResource getIconBus17NorthWest();

  @Source("MapIcon-Bus-17-SouthWest.png")
  public ImageResource getIconBus17SouthWest();

  @Source("MapIcon-Bus-17-NorthEast.png")
  public ImageResource getIconBus17NorthEast();

  @Source("MapIcon-Bus-17-SouthEast.png")
  public ImageResource getIconBus17SouthEast();

  @Source("MapIcon-Bus-22.png")
  public ImageResource getIconBus22();

  @Source("MapIcon-Bus-22-North.png")
  public ImageResource getIconBus22North();

  @Source("MapIcon-Bus-22-South.png")
  public ImageResource getIconBus22South();

  @Source("MapIcon-Bus-22-East.png")
  public ImageResource getIconBus22East();

  @Source("MapIcon-Bus-22-West.png")
  public ImageResource getIconBus22West();

  @Source("MapIcon-Bus-22-NorthWest.png")
  public ImageResource getIconBus22NorthWest();

  @Source("MapIcon-Bus-22-SouthWest.png")
  public ImageResource getIconBus22SouthWest();

  @Source("MapIcon-Bus-22-NorthEast.png")
  public ImageResource getIconBus22NorthEast();

  @Source("MapIcon-Bus-22-SouthEast.png")
  public ImageResource getIconBus22SouthEast();

  /****
   * Map Icons - Light Rail
   ****/

  @Source("MapIcon-LightRail-10.png")
  public ImageResource getIconLightRail10();

  @Source("MapIcon-LightRail-14.png")
  public ImageResource getIconLightRail14();

  @Source("MapIcon-LightRail-17.png")
  public ImageResource getIconLightRail17();

  @Source("MapIcon-LightRail-22.png")
  public ImageResource getIconLightRail22();

  /****
   * Map Icons - Rail
   ****/

  @Source("MapIcon-Rail-10.png")
  public ImageResource getIconRail10();

  @Source("MapIcon-Rail-14.png")
  public ImageResource getIconRail14();

  @Source("MapIcon-Rail-17.png")
  public ImageResource getIconRail17();

  @Source("MapIcon-Rail-22.png")
  public ImageResource getIconRail22();

  /****
   * Map Icons - Ferry
   ****/

  @Source("MapIcon-Ferry-10.png")
  public ImageResource getIconFerry10();

  @Source("MapIcon-Ferry-14.png")
  public ImageResource getIconFerry14();

  @Source("MapIcon-Ferry-17.png")
  public ImageResource getIconFerry17();

  @Source("MapIcon-Ferry-22.png")
  public ImageResource getIconFerry22();

  /****
   * Routes
   ****/

  @Source("RouteStart.png")
  public DataResource getImageRouteStart();

  @Source("RouteEnd.png")
  public DataResource getImageRouteEnd();

  @Source("SelectionCircle36.png")
  public ImageResource getSelectionCircle36();

  @Source("SelectionCircle30.png")
  public ImageResource getSelectionCircle30();
}

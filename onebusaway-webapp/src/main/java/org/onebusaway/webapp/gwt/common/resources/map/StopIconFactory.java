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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.resources.client.ImageResource;

public class StopIconFactory {

  public enum ESize {
    TINY, SMALL, MEDIUM, LARGE
  };

  public enum EType {
    BUS, LIGHT_RAIL, RAIL, FERRY
  }

  private static MapResources _r = MapResources.INSTANCE;

  private static Map<String, IconInfo> _iconsByType = new HashMap<String, IconInfo>();

  static {

    String[] directions = {"", "N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    IconInfo tinyBusInfo = new IconInfo(_r.getIconBus10());
    IconInfo smallBusInfo = new IconInfo(_r.getIconBus14());

    IconInfo lightRail10 = new IconInfo(_r.getIconLightRail10());
    IconInfo lightRail14 = new IconInfo(_r.getIconLightRail14());
    IconInfo lightRail17 = new IconInfo(_r.getIconLightRail17());
    IconInfo lightRail22 = new IconInfo(_r.getIconLightRail22());

    IconInfo rail10 = new IconInfo(_r.getIconRail10());
    IconInfo rail14 = new IconInfo(_r.getIconRail14());
    IconInfo rail17 = new IconInfo(_r.getIconRail17());
    IconInfo rail22 = new IconInfo(_r.getIconRail22());

    IconInfo ferry10 = new IconInfo(_r.getIconFerry10());
    IconInfo ferry14 = new IconInfo(_r.getIconFerry14());
    IconInfo ferry17 = new IconInfo(_r.getIconFerry17());
    IconInfo ferry22 = new IconInfo(_r.getIconFerry22());

    // For the super tiny icons, we use the same icon for all directions and
    // vehicle types
    for (String direction : directions) {

      putIconInfo(ESize.TINY, EType.BUS, direction, tinyBusInfo);
      putIconInfo(ESize.SMALL, EType.BUS, direction, smallBusInfo);

      putIconInfo(ESize.TINY, EType.LIGHT_RAIL, direction, lightRail10);
      putIconInfo(ESize.SMALL, EType.LIGHT_RAIL, direction, lightRail14);
      putIconInfo(ESize.MEDIUM, EType.LIGHT_RAIL, direction, lightRail17);
      putIconInfo(ESize.LARGE, EType.LIGHT_RAIL, direction, lightRail22);

      putIconInfo(ESize.TINY, EType.RAIL, direction, rail10);
      putIconInfo(ESize.SMALL, EType.RAIL, direction, rail14);
      putIconInfo(ESize.MEDIUM, EType.RAIL, direction, rail17);
      putIconInfo(ESize.LARGE, EType.RAIL, direction, rail22);

      putIconInfo(ESize.TINY, EType.FERRY, direction, ferry10);
      putIconInfo(ESize.SMALL, EType.FERRY, direction, ferry14);
      putIconInfo(ESize.MEDIUM, EType.FERRY, direction, ferry17);
      putIconInfo(ESize.LARGE, EType.FERRY, direction, ferry22);
    }

    putIconInfo(ESize.MEDIUM, EType.BUS, "", _r.getIconBus17());
    putIconInfo(ESize.MEDIUM, EType.BUS, "N", _r.getIconBus17North());
    putIconInfo(ESize.MEDIUM, EType.BUS, "S", _r.getIconBus17South());
    putIconInfo(ESize.MEDIUM, EType.BUS, "E", _r.getIconBus17East());
    putIconInfo(ESize.MEDIUM, EType.BUS, "W", _r.getIconBus17West());
    putIconInfo(ESize.MEDIUM, EType.BUS, "NE", _r.getIconBus17NorthEast());
    putIconInfo(ESize.MEDIUM, EType.BUS, "SE", _r.getIconBus17SouthEast());
    putIconInfo(ESize.MEDIUM, EType.BUS, "NW", _r.getIconBus17NorthWest());
    putIconInfo(ESize.MEDIUM, EType.BUS, "SW", _r.getIconBus17SouthWest());

    putIconInfo(ESize.LARGE, EType.BUS, "", _r.getIconBus22());
    putIconInfo(ESize.LARGE, EType.BUS, "N", _r.getIconBus22North());
    putIconInfo(ESize.LARGE, EType.BUS, "S", _r.getIconBus22South());
    putIconInfo(ESize.LARGE, EType.BUS, "E", _r.getIconBus22East());
    putIconInfo(ESize.LARGE, EType.BUS, "W", _r.getIconBus22West());
    putIconInfo(ESize.LARGE, EType.BUS, "NE", _r.getIconBus22NorthEast());
    putIconInfo(ESize.LARGE, EType.BUS, "SE", _r.getIconBus22SouthEast());
    putIconInfo(ESize.LARGE, EType.BUS, "NW", _r.getIconBus22NorthWest());
    putIconInfo(ESize.LARGE, EType.BUS, "SW", _r.getIconBus22SouthWest());
  }

  public static Icon getRouteStartIcon() {
    return getRouteIcon(_r.getImageRouteStart().getUrl());
  }

  public static Icon getRouteEndIcon() {
    return getRouteIcon(_r.getImageRouteEnd().getUrl());
  }

  public static Marker getStopSelectionCircle(LatLng p, boolean bigger) {
    ImageResource resource = bigger ? _r.getSelectionCircle36()
        : _r.getSelectionCircle30();

    Icon icon = Icon.newInstance();
    icon.setImageURL(resource.getURL());

    int w = resource.getWidth();
    int h = resource.getHeight();
    int w2 = w / 2;
    int h2 = h / 2;

    icon.setIconSize(Size.newInstance(w, h));
    icon.setIconAnchor(Point.newInstance(w2, h2));
    icon.setInfoWindowAnchor(Point.newInstance(w2, h2));

    MarkerOptions options = MarkerOptions.newInstance(icon);
    return new Marker(p, options);
  }

  public static Icon getStopIcon(StopBean stop, ESize size, boolean isSelected) {
    EType type = getStopType(stop);
    String direction = stop.getDirection();
    if (direction == null)
      direction = "";
    IconInfo info = getIconInfo(size, type, direction);
    return info.getIcon(isSelected);
  }

  public static EType getStopType(StopBean stop) {
    Set<Integer> types = new HashSet<Integer>();
    for (RouteBean route : stop.getRoutes())
      types.add(route.getType());

    // Ferry takes precedent
    if (types.contains(4))
      return EType.FERRY;
    // Followed by heavy rail
    else if (types.contains(2))
      return EType.RAIL;
    // Followed by light-rail
    else if (types.contains(0))
      return EType.LIGHT_RAIL;
    // Bus by default
    else
      return EType.BUS;
  }

  /*****
   * Private Methods
   ****/

  private static Icon getRouteIcon(String url) {
    Icon icon = Icon.newInstance();
    icon.setImageURL(url);
    icon.setIconSize(Size.newInstance(20, 34));
    icon.setIconAnchor(Point.newInstance(10, 34));
    return icon;
  }

  private static void putIconInfo(ESize size, EType type, String direction,
      ImageResource resource) {
    putIconInfo(size, type, direction, new IconInfo(resource));
  }

  private static void putIconInfo(ESize size, EType type, String direction,
      IconInfo info) {
    String key = key(size, type, direction);
    _iconsByType.put(key, info);
  }

  private static IconInfo getIconInfo(ESize size, EType type, String direction) {
    String key = key(size, type, direction);
    return _iconsByType.get(key);
  }

  private static String key(ESize size, EType type, String direction) {
    return size.toString() + "-" + type + "-" + direction;
  }

  private static class IconInfo {

    private ImageResource _resource;

    public IconInfo(ImageResource resource) {
      _resource = resource;
    }

    public Icon getIcon(boolean isSelected) {

      Icon icon = Icon.newInstance();

      icon.setImageURL(_resource.getURL());

      int w = _resource.getWidth();
      int h = _resource.getHeight();
      int w2 = w / 2;
      int h2 = h / 2;

      icon.setIconSize(Size.newInstance(w, h));
      icon.setIconAnchor(Point.newInstance(w2, h2));
      icon.setInfoWindowAnchor(Point.newInstance(w2, h2));
      if (isSelected) {
        // icon.setShadowURL(r.getImageSelectedStop().getUrl());
        // icon.setShadowSize(Size.newInstance(22, 21));
      }

      return icon;
    }
  }

}

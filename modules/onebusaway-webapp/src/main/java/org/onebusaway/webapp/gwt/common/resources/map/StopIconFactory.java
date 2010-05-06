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
package org.onebusaway.webapp.gwt.common.resources.map;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.resources.client.ImageResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StopIconFactory {

  public enum ESize {
    TINY, SMALL, MEDIUM, LARGE
  };

  public enum EType {
    BUS, LIGHT_RAIL, RAIL
  }

  private static MapResources _r = MapResources.INSTANCE;

  private static Map<String, IconInfo> _iconsByType = new HashMap<String, IconInfo>();

  static {

    String[] directions = {"", "N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    IconInfo tinyBusInfo = new IconInfo(_r.getIconBus10());
    IconInfo smallBusInfo = new IconInfo(_r.getIconBus14());
    
    IconInfo lightRail17 = new IconInfo(_r.getIconLightRail17());
    IconInfo lightRail22 = new IconInfo(_r.getIconLightRail22());
    
    IconInfo rail17 = new IconInfo(_r.getIconRail17());
    IconInfo rail22 = new IconInfo(_r.getIconRail22());

    // For the super tiny icons, we use the same icon for all directions and
    // vehicle types
    for (String direction : directions) {
      for (EType type : EType.values()) {
        putIconInfo(ESize.TINY, type, direction, tinyBusInfo);
        putIconInfo(ESize.SMALL, type, direction, smallBusInfo);
      }
      
      putIconInfo(ESize.MEDIUM, EType.LIGHT_RAIL, direction, lightRail17);
      putIconInfo(ESize.LARGE, EType.LIGHT_RAIL, direction, lightRail22);
      
      putIconInfo(ESize.MEDIUM, EType.RAIL, direction, rail17);
      putIconInfo(ESize.LARGE, EType.RAIL, direction, rail22);
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
    for( RouteBean route : stop.getRoutes() )
      types.add(route.getType());
    
    // Heay rail dominations
    if( types.contains(2))
      return EType.RAIL;
    // Followed by ligh-rail
    else if( types.contains(0))
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

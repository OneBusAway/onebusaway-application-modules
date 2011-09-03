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
package org.onebusaway.webapp.gwt.mobile_application;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.mobile_application.control.ActivityHandler;
import org.onebusaway.webapp.gwt.mobile_application.control.LocationManager;
import org.onebusaway.webapp.gwt.mobile_application.control.MobileApplicationDao;
import org.onebusaway.webapp.gwt.mobile_application.view.MapViewController;
import org.onebusaway.webapp.gwt.viewkit.TabBarController;

public class MobileApplicationContext {

  private static MobileApplicationDao _dao = new MobileApplicationDao();

  private static ActivityHandler _activityHandler = new ActivityHandlerImpl();

  private static LocationManager _locationManager = new LocationManager();
  
  private static TabBarController _rootController = new TabBarController();
  
  private static MapViewController _mapViewController = new MapViewController();

  public static MobileApplicationDao getDao() {
    return _dao;
  }

  public static ActivityHandler getActivityHandler() {
    return _activityHandler;
  }

  public static LocationManager getLocationManager() {
    return _locationManager;
  }

  private static class ActivityHandlerImpl implements ActivityHandler {

    @Override
    public void onStopAccessed(StopBean stop) {
      _dao.addRecentStop(stop);
    }
  }

  public static TabBarController getRootController() {
    return _rootController;
  }
  
  public static MapViewController getMapViewController() {
    return _mapViewController;
  }
}

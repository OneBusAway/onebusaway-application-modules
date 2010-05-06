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

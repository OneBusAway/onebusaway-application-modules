package org.onebusaway.where.impl;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.web.common.client.model.NameBean;
import org.onebusaway.where.web.common.client.model.StopBean;

public class ApplicationBeanLibrary {

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  public static StopBean getStopAsBean(Stop stop) {
    return fillStopBean(stop, new StopBean());
  }

  public static StopBean fillStopBean(Stop stop, StopBean bean) {
    bean.setId(stop.getId());
    bean.setLat(stop.getLat());
    bean.setLon(stop.getLon());
    if (stop.getDirection() != null)
      bean.setDirection(getStopDirection(stop));
    bean.setName(stop.getName());
    return bean;
  }

  public static NameBean getNameAsBean(SelectionName name) {
    return new NameBean(name.getType(), name.getNames());
  }

  public static String getStopDirection(Stop stop) {

    double theta = stop.getDirection();
    double t = Math.PI / 4;

    int r = (int) Math.floor((theta + t / 2) / t);

    switch (r) {
      case 0:
        return "E";
      case 1:
        return "NE";
      case 2:
        return "N";
      case 3:
        return "NW";
      case 4:
        return "W";
      case -1:
        return "SE";
      case -2:
        return "S";
      case -3:
        return "SW";
      case -4:
        return "W";
      default:
        return "?";
    }
  }

}

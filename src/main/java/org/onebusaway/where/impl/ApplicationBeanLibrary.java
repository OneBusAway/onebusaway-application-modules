package org.onebusaway.where.impl;

import com.vividsolutions.jts.geom.Point;

import java.util.List;

import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.web.common.client.model.NameBean;

public class ApplicationBeanLibrary {

  public static String getBestName(String... names) {
    for (String name : names) {
      name = name == null ? "" : name.trim();
      if (name.length() > 0)
        return name;
    }
    return "";
  }

  public static StopBean getStopAsBean(Stop stop) {
    return fillStopBean(stop, new StopBean());
  }

  public static StopBean fillStopBean(Stop stop, StopBean bean) {
    bean.setId(stop.getId());
    bean.setLat(stop.getLat());
    bean.setLon(stop.getLon());
    Point p = stop.getLocation();
    bean.setX(p.getX());
    bean.setY(p.getY());
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

  public static PathBean getShapePointsAsPathBean(List<ShapePoint> points) {

    double[] lat = new double[points.size()];
    double[] lon = new double[points.size()];

    int index = 0;

    for (ShapePoint point : points) {
      lat[index] = point.getLat();
      lon[index] = point.getLon();
      index++;
    }

    return new PathBean(lat, lon);
  }
}

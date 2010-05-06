package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.PathBean;

import java.util.List;

public class ApplicationBeanLibrary {

  public static String getId(AgencyAndId id) {
    return id.getAgencyId() + "_" + id.getId();
  }

  public static String getBestName(String... names) {
    for (String name : names) {
      name = name == null ? "" : name.trim();
      if (name.length() > 0)
        return name;
    }
    return "";
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

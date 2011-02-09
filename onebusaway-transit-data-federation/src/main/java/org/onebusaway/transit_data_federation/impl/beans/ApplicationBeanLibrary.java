package org.onebusaway.transit_data_federation.impl.beans;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.transit_data.model.PathBean;

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

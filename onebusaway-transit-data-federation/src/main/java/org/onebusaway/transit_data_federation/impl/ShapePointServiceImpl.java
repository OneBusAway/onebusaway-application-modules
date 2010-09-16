package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.ShapePointService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class ShapePointServiceImpl implements ShapePointService {

  private GtfsRelationalDao _gtfsDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Cacheable
  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId) {

    List<ShapePoint> shapePoints = _gtfsDao.getShapePointsForShapeId(shapeId);

    if (shapePoints.isEmpty())
      return null;

    int n = shapePoints.size();

    double[] lat = new double[n];
    double[] lon = new double[n];
    double[] distTraveled = new double[n];

    int i = 0;
    for (ShapePoint shapePoint : shapePoints) {
      lat[i] = shapePoint.getLat();
      lon[i] = shapePoint.getLon();
      distTraveled[i] = shapePoint.getDistTraveled();
      i++;
    }

    ShapePoints result = new ShapePoints();
    result.setShapeId(shapeId);
    result.setLats(lat);
    result.setLons(lon);
    result.setDistTraveled(distTraveled);
    return result;
  }

  @Cacheable
  @Override
  public ShapePoints getShapePointsForShapeIds(List<AgencyAndId> shapeIds) {
    ShapePointsFactory factory = new ShapePointsFactory();
    for (AgencyAndId shapeId : shapeIds) {
      ShapePoints shapePoints = getShapePointsForShapeId(shapeId);
      factory.addPoints(shapePoints);
    }
    return factory.create();
  }
}

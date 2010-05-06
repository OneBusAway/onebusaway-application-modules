package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.exceptions.InvalidArgumentServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.StopSearchService;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;

import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
class StopsBeanServiceImpl implements StopsBeanService {

  @Autowired
  private StopSearchService _searchService;

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private GeospatialBeanService _geospatialBeanService;

  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2, int maxCount) throws ServiceException {

    List<AgencyAndId> stopIds = _geospatialBeanService.getStopsByBounds(lat1,lon1,lat2,lon2);

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(stopIds, maxCount);
    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (AgencyAndId stopId : stopIds) {
      StopBean stopBean = _stopBeanService.getStopForId(stopId);
      if (stopBean == null)
        throw new ServiceException();

      stopBeans.add(stopBean);
    }

    return constructResult(stopBeans, limitExceeded);
  }

  public StopsBean getStopsByBoundsAndQuery(double lat1, double lon1,
      double lat2, double lon2, String query, int maxCount)
      throws ServiceException {

    CoordinateRectangle bounds = new CoordinateRectangle(lat1, lon1, lat2, lon2);
    CoordinatePoint center = bounds.getCenter();

    SearchResult<AgencyAndId> stops;
    try {
      stops = _searchService.searchForStopsByCode(query, 10);
    } catch (ParseException e) {
      throw new InvalidArgumentServiceException("query", "queryParseError");
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServiceException();
    }

    Min<StopBean> closest = new Min<StopBean>();
    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (AgencyAndId aid : stops.getResults()) {
      StopBean stopBean = _stopBeanService.getStopForId(aid);
      if (bounds.contains(stopBean.getLat(), stopBean.getLon()))
        stopBeans.add(stopBean);
      double distance = SphericalGeometryLibrary.distance(center.getLat(),
          center.getLon(), stopBean.getLat(), stopBean.getLon());
      closest.add(distance, stopBean);
    }

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(stopBeans, maxCount);

    // If nothing was found in range, add the closest result
    if (stopBeans.isEmpty() && !closest.isEmpty())
      stopBeans.add(closest.getMinElement());

    return constructResult(stopBeans, limitExceeded);
  }

  private StopsBean constructResult(List<StopBean> stopBeans,
      boolean limitExceeded) {
    
    Collections.sort(stopBeans, new StopBeanIdComparator());
    
    StopsBean result = new StopsBean();
    result.setStopBeans(stopBeans);
    result.setLimitExceeded(limitExceeded);
    return result;
  }
}

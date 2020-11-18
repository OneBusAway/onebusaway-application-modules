/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.onebusaway.collections.Min;
import org.onebusaway.exceptions.InvalidArgumentServiceException;
import org.onebusaway.exceptions.NoSuchAgencyServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.StopSearchService;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopsBeanServiceImpl implements StopsBeanService {

  private static Logger _log = LoggerFactory.getLogger(StopsBeanServiceImpl.class);

  private static final double MIN_SCORE = 1.0;
  private static final double NAME_MIN_SCORE = 4.0;
  private static final int MAX_STOPS = 10;

  @Autowired
  private StopSearchService _searchService;

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private GeospatialBeanService _geospatialBeanService;

  @Autowired
  private TransitGraphDao _transitGraphDao;

  @Override
  public StopsBean getStops(SearchQueryBean queryBean) throws ServiceException {
    String query = queryBean.getQuery();
    if (query == null)
      return getStopsByBounds(queryBean);
    else
      return getStopsByBoundsAndQuery(queryBean);
  }

  @Override
  public StopsBean getStopsByName(String stopName) throws ServiceException {
    List<StopBean> stopBeans = new ArrayList<StopBean>();
    SearchResult<AgencyAndId> results = null;
    try {

      results =
       _searchService.searchForStopsByName(stopName,
              MAX_STOPS, NAME_MIN_SCORE);

      for (AgencyAndId aid : results.getResultsByTopScore()) {
        StopBean stopBean = _stopBeanService.getStopForId(aid, null);
        if (stopBean != null) {
          stopBeans.add(stopBean);
        }
      }
    } catch (Exception e) {
      _log.error("search failed!", e);
      // simply return no results, the search was not understood
      return new StopsBean();
    }
    if (results == null) {
      return new StopsBean();
    }
    return constructResult(stopBeans, results.size() == MAX_STOPS);
  }

  private StopsBean getStopsByBounds(SearchQueryBean queryBean)
      throws ServiceException {

    CoordinateBounds bounds = queryBean.getBounds();

    List<AgencyAndId> stopIds = _geospatialBeanService.getStopsByBounds(bounds);

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(stopIds,
        queryBean.getMaxCount());
    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (AgencyAndId stopId : stopIds) {
      StopBean stopBean = _stopBeanService.getStopForId(stopId, null);
      if (stopBean == null)
        throw new ServiceException();

      /**
       * If the stop doesn't have any routes actively serving it, don't include
       * it in the results
       */
      if (stopBean.getRoutes().isEmpty())
        continue;

      stopBeans.add(stopBean);
    }

    return constructResult(stopBeans, limitExceeded);
  }

  private StopsBean getStopsByBoundsAndQuery(SearchQueryBean queryBean)
      throws ServiceException {

    CoordinateBounds bounds = queryBean.getBounds();
    String query = queryBean.getQuery();
    int maxCount = queryBean.getMaxCount();

    CoordinatePoint center = SphericalGeometryLibrary.getCenterOfBounds(bounds);

    SearchResult<AgencyAndId> stops;
    try {
      stops = _searchService.searchForStopsByCode(query, 10, MIN_SCORE);
    } catch (ParseException e) {
      throw new InvalidArgumentServiceException("query", "queryParseError");
    } catch (IOException e) {
      _log.error("error executing stop search: query=" + query, e);
      e.printStackTrace();
      throw new ServiceException();
    }

    Min<StopBean> closest = new Min<StopBean>();
    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (AgencyAndId aid : stops.getResults()) {
      StopBean stopBean = _stopBeanService.getStopForId(aid, null);
      if (bounds.contains(stopBean.getLat(), stopBean.getLon()))
        stopBeans.add(stopBean);
      double distance = SphericalGeometryLibrary.distance(center.getLat(),
          center.getLon(), stopBean.getLat(), stopBean.getLon());
      closest.add(distance, stopBean);
    }

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(stopBeans,
        maxCount);

    // If nothing was found in range, add the closest result
    if (stopBeans.isEmpty() && !closest.isEmpty())
      stopBeans.add(closest.getMinElement());

    return constructResult(stopBeans, limitExceeded);
  }

  @Override
  public ListBean<String> getStopsIdsForAgencyId(String agencyId) {
    AgencyEntry agency = _transitGraphDao.getAgencyForId(agencyId);
    if (agency == null)
      throw new NoSuchAgencyServiceException(agencyId);
    List<String> ids = new ArrayList<String>();
    for (StopEntry stop : agency.getStops()) {
      AgencyAndId id = stop.getId();
      ids.add(AgencyAndIdLibrary.convertToString(id));
    }
    return new ListBean<String>(ids, false);
  }

  private StopsBean constructResult(List<StopBean> stopBeans,
      boolean limitExceeded) {

    Collections.sort(stopBeans, new StopBeanIdComparator());

    StopsBean result = new StopsBean();
    result.setStops(stopBeans);
    result.setLimitExceeded(limitExceeded);
    return result;
  }

}

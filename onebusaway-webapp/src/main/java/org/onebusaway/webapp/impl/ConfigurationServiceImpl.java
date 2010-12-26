package org.onebusaway.webapp.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.impl.ServletLibrary;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationServiceImpl implements ConfigurationService {

  private TransitDataService _transitDataService;

  private ServletContext _servletContext;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setServletContext(ServletContext servletContext) {
    _servletContext = servletContext;
  }

  @Override
  @Cacheable
  public Map<String, Object> getConfiguration(
      @CacheableArgument(cacheRefreshIndicator = true) boolean forceRefresh) {

    Map<String, Object> config = new HashMap<String, Object>();
    config.put("apiKey", "web");

    String contextPath = ServletLibrary.getContextPath(_servletContext);
    config.put("apiUrl", contextPath + "/api");

    List<AgencyWithCoverageBean> agenciesWithCoverage = _transitDataService.getAgenciesWithCoverage();

    CoordinateBounds bounds = new CoordinateBounds();

    for (AgencyWithCoverageBean awc : agenciesWithCoverage) {
      bounds.addPoint(awc.getLat() + awc.getLatSpan() / 2,
          awc.getLon() + awc.getLonSpan() / 2);
      bounds.addPoint(awc.getLat() - awc.getLatSpan() / 2,
          awc.getLon() - awc.getLonSpan() / 2);
    }

    if (bounds.isEmpty()) {
      config.put("centerLat", 0.0);
      config.put("centerLon", 0.0);
      config.put("spanLat", 180.0);
      config.put("spanLon", 180.0);
    } else {
      config.put("centerLat", (bounds.getMinLat() + bounds.getMaxLat()) / 2);
      config.put("centerLon", (bounds.getMinLon() + bounds.getMaxLon()) / 2);
      config.put("spanLat", bounds.getMaxLat() - bounds.getMinLat());
      config.put("spanLon", bounds.getMaxLon() - bounds.getMinLon());
    }

    return config;
  }

}

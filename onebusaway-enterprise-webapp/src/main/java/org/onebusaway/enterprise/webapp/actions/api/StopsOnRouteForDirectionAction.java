package org.onebusaway.enterprise.webapp.actions.api;

import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.api.model.StopOnRoute;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback"})
public class StopsOnRouteForDirectionAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _transitDataService;

  private List<StopOnRoute> _stops = new ArrayList<StopOnRoute>();

  private String _routeId = null;

  private String _directionId = null;

  public void setRouteId(String routeId) {
    _routeId = routeId;
  }

  public void setDirectionId(String directionId) {
    _directionId = directionId;
  }

  @Override
  public String execute() {    
    if(_routeId == null) {
      return SUCCESS;
    }
    
    StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(_routeId);

    // create stop ID->stop bean map
    Map<String, StopBean> stopIdToStopBeanMap = new HashMap<String, StopBean>();
    for(StopBean stopBean : stopsForRoute.getStops()) {
      stopIdToStopBeanMap.put(stopBean.getId(), stopBean);
    }   
    
    // break up stops into destinations
    List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
    for (StopGroupingBean stopGroupingBean : stopGroupings) {
      for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
        NameBean name = stopGroupBean.getName();
        String type = name.getType();

        if (!type.equals("destination") || !stopGroupBean.getId().equals(_directionId))
          continue;
        
        if(!stopGroupBean.getStopIds().isEmpty()) {
          for(String stopId : stopGroupBean.getStopIds()) {
            _stops.add(new StopOnRoute(stopIdToStopBeanMap.get(stopId)));
          }
        }
      }
    }
    
    return SUCCESS;
  }   

  /** 
   * VIEW METHODS
   */
  public List<StopOnRoute> getStops() {
    return _stops;
  }

}



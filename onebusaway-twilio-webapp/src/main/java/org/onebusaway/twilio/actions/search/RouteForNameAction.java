/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.twilio.actions.search;

import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Results({
	  @Result(name="success", location="stops-for-route", type="chain"),
		// redirect to prevent loop
	  @Result(name="multipleRoutesFound", type="redirectAction", params={"From", "${phoneNumber}", "namespace", "/search", "actionName", "multiple-routes-found"}),
    @Result(name="noRoutesFound", type="redirectAction", params={"From", "${phoneNumber}", "namespace", "/", "actionName", "message-and-back"})
})
public class RouteForNameAction extends TwilioSupport implements SessionAware {
	  private static final long serialVersionUID = 1L;
	  private static Logger _log = LoggerFactory.getLogger(IndexAction.class);

	  private String _routeName;
	  private RouteBean _route;
	  private List<RouteBean> _routes;
	  private Map sessionMap;
	  
	  public void setRouteName(String routeName) {
	    _routeName = routeName;
	  }

	  public String getRouteName() {
	    return _routeName;
	  }

	  public RouteBean getRoute() {
	    return _route;
	  }

	  public List<RouteBean> getRoutes() {
	    return _routes;
	  }
	  
	  public void setSession(Map map) {
	  	  this.sessionMap = map;
	  }
		
	  public String execute() throws Exception {
	    _log.debug("in RouteForName with routeName " + _routeName); 
		  
	    CoordinateBounds bounds = getDefaultSearchArea();
	    
	    if( bounds == null) {
	      return NEEDS_DEFAULT_SEARCH_LOCATION;
	    }
	    
	    if( _routeName == null || _routeName.length() == 0) {
	    	return INPUT;
	    }

	    SearchQueryBean routesQuery = new SearchQueryBean();
	    routesQuery.setBounds(bounds);
	    routesQuery.setMaxCount(10);
	    routesQuery.setQuery(_routeName);
	    routesQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
	    
	    RoutesBean routesBean = _transitDataService.getRoutes(routesQuery);
	    List<RouteBean> routes = routesBean.getRoutes();
	    sessionMap.put("navState", new Integer(DISPLAY_DATA));	      
	    
	    logUserInteraction("route", _routeName);

	    if (routes.size() == 0) {
        sessionMap.put("messageFromAction", getText(Messages.NO_ROUTES_WERE_FOUND));
        sessionMap.put("backAction", "search-index");
	      return "noRoutesFound";
	    } else if (routes.size() == 1 ) {
	      _route = routes.get(0);
	      return SUCCESS;
	    } else {
	      _routes = routes;
				sessionMap.put("routes", routes);
				return "multipleRoutesFound";
	    }
	  }
}

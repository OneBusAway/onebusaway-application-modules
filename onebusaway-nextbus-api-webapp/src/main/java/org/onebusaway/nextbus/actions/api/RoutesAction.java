package org.onebusaway.nextbus.actions.api;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.nextbus.model.Route;
import org.onebusaway.nextbus.model.Routes;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.comparators.AlphanumComparator;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RoutesAction implements ModelDriven<Routes> {
	
	@Autowired
	private TransitDataService _service;
	
	
	public TransitDataService get_service() {
		return _service;
	}

	public void set_service(TransitDataService _service) {
		this._service = _service;
	}
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public Routes getModel() {
		Routes routes = new Routes();
		Set<RouteBean> routeBeans = new TreeSet<RouteBean>(new RouteComparator());
		
		/*SearchQueryBean searchQuery = new SearchQueryBean();
		searchQuery.setBounds(SphericalGeometryLibrary.bounds(27.950535, -82.457190, 10000));
	    searchQuery.setMaxCount(Integer.MAX_VALUE);*/
	    //searchQuery.setType(EQueryType.BOUNDS);
	
		for(AgencyWithCoverageBean agencyBean : _service.getAgenciesWithCoverage()){
			for(RouteBean routeBean : _service.getRoutesForAgencyId(agencyBean.getAgency().getId()).getList()){
				routeBeans.add(routeBean);
			}
		}
		
		for(RouteBean routeBean : routeBeans){
			Route route = new Route();
			route.setName(routeBean.getLongName());
			route.setRouteId(routeBean.getId());
			
			routes.getRoutes().add(route);
		}

		return routes;
		
	}
	
	class RouteComparator implements Comparator<RouteBean>
	{
		private Comparator<String> alphaNumComparator = new AlphanumComparator();
	    @Override
	    public int compare(RouteBean r1, RouteBean r2)
	    {
	    	if (r1.getShortName() != null && r2.getShortName() != null) {
	        	return alphaNumComparator.compare(r1.getId(), r2.getId());
	        } else {
	            return r1.getId().compareTo(r2.getId());
	        }
	    }
	}

}

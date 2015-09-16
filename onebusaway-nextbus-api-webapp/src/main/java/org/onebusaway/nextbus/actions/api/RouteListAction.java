package org.onebusaway.nextbus.actions.api;

import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.nextbus.model.Body;
import org.onebusaway.nextbus.model.DisplayRoute;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RouteListAction implements ModelDriven<Body<DisplayRoute>>{
	
	@Autowired
	private TransitDataService _service;
	
	private String agencyId;
	
	public String getA() {
		return agencyId;
	}
	
	@RequiredFieldValidator
	public void setA(String agencyId) {
		this.agencyId = agencyId;
	}
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}
	
	@Override
	public Body<DisplayRoute> getModel() {
		
		Body<DisplayRoute> body = new Body<DisplayRoute>();
		if(agencyId !=null){
			List<RouteBean> routeBeans = _service.getRoutesForAgencyId(agencyId).getList();
			for(RouteBean routeBean : routeBeans){
				DisplayRoute route = new DisplayRoute();
				route.setTag(routeBean.getId());
				route.setTitle(routeBean.getLongName());
				route.setShortTitle(routeBean.getShortName());
				body.getResponse().add(route);
			}
		}
		return body;
		
	}

}

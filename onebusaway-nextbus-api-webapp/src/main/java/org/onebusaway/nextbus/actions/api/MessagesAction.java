/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.nextbus.Message;
import org.onebusaway.nextbus.model.nextbus.MessageText;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class MessagesAction extends NextBusApiBase implements ModelDriven<Body<Message>>{
	
	@Autowired
	private TransitDataService _service;
	
	private String agencyId;
	
	private String routeId;
	
	public String getA() {
		return agencyId;
	}
	
	@RequiredFieldValidator
	public void setA(String agencyId) {
		this.agencyId = agencyId;
	}
	
	public String getR() {
		return routeId;
	}

	public void setR(String routeId) {
		this.routeId = routeId;
	}

	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}
	
	@Override
	public Body<Message> getModel() {
		
		Body<Message> body = new Body<Message>();
		
	    List<String> agencyIds = processAgencyIds(getA());
		
		List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
		
		if(processRouteIds(routeId, routeIds, agencyIds, body)){
			for(AgencyAndId routeId : routeIds){
				body.getResponse().addAll(getMessagesForRoute(agencyId,routeId.toString()));
			}
		}
		else{
			body.getErrors().add(new BodyError("You must provide a route id."));
		}
	
		return body;
		
	}
	
	private List<Message> getMessagesForRoute(String agencyId, String routeId){
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(6);
		
		List<Message> messageList = new ArrayList<Message>(); 
		ListBean<ServiceAlertBean> serviceAlertBeans = getAllSituations(agencyId, routeId);
		    for(ServiceAlertBean serviceAlert : serviceAlertBeans.getList()) {
		      //serviceAlertBean.get
		      
		      Message message = new Message();
		      message.setId(serviceAlert.getId());
		      message.setCreator(serviceAlert.getReason());
		      
		      message.setEndBoundaryStr(serviceAlert.getPublicationWindows().toString());
		      
		      message.setMessageText(new MessageText(serviceAlert.getDescriptions().toString()));
		      
		      messageList.add(message);
		    }
		    
		    return messageList;
		    
	 }
	
	private ListBean<ServiceAlertBean> getAllSituations(String agencyId, String routeId) {
		 // First get service alerts for the stop
        SituationQueryBean query = new SituationQueryBean();
        /*List<String> stopIdStrings = new ArrayList<String>();
        stopIdStrings.add(stopIdString);*/
        SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
        query.getAffects().add(affects);
        affects.setAgencyId(agencyId);
        affects.setRouteId(routeId);
        
	    return _transitDataService.getServiceAlerts(query);
  }

}

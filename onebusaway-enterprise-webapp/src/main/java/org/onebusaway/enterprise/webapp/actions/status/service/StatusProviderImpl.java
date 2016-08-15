/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.enterprise.webapp.actions.status.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.onebusaway.enterprise.webapp.actions.status.model.IcingaItem;
import org.onebusaway.enterprise.webapp.actions.status.model.IcingaResponse;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusGroup;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusItem;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean.AffectsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class StatusProviderImpl implements StatusProvider {

  private static Logger _log = LoggerFactory.getLogger(StatusProviderImpl.class);
  
  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private ConfigurationService _config;
  
  @Override
  public StatusGroup getServiceAlertStatus() {

    StatusGroup group = new StatusGroup();
    group.setTitle("Service Alerts");

    List<AgencyWithCoverageBean> agencies = _transitDataService.getAgenciesWithCoverage();

    for (AgencyWithCoverageBean agency : agencies) {

      String agencyId = agency.getAgency().getId();
      String agencyName = agency.getAgency().getName();
      
      // Use query to limit to agency and no other parameters
      SituationQueryBean query = new SituationQueryBean();
      AffectsBean ab = new AffectsBean();
      ab.setAgencyId(agencyId);
      query.setAffects(Collections.singletonList(ab));
      ListBean<ServiceAlertBean> alerts = _transitDataService.getServiceAlerts(
          query);

      for (ServiceAlertBean bean : alerts.getList()) {
        StatusItem item = new StatusItem();
        item.setDescription(bean.getDescriptions().get(0).getValue());
        item.setTitle(agencyName + ": " + bean.getSummaries().get(0).getValue());
        item.setStatus(StatusItem.Status.INFO);
        group.addItem(item);
      }
    }
    
    return group;
  }
  
  @Override
  public StatusGroup getIcingaStatus() {
    
    StatusGroup group = new StatusGroup();
    group.setTitle("Icinga Alerts");
    
    IcingaResponse response = null;
    
    // fail if not configured!
    String baseUrl = _config.getConfigurationValueAsString("icinga.baseUrl", null);
    String command = _config.getConfigurationValueAsString("icinga.command", null);
        
    try {
      HttpClient client = new HttpClient();
      String url = baseUrl + encode(command);
      HttpMethod method = new GetMethod(url);
      client.executeMethod(method);
      InputStream result = method.getResponseBodyAsStream();
      ObjectMapper mapper = new ObjectMapper();
      response = mapper.readValue(result, IcingaResponse.class);
    } catch (IOException e) {
      _log.error("Exception getting Icinga data " + e);
      e.printStackTrace();
    }
    
    if (response == null) {
      return group;
    }
    
    for (IcingaItem bean : response.getResult()) {
      StatusItem item = new StatusItem();
      item.setDescription(bean.getServiceName());
      item.setTitle(bean.getServiceDisplayName());
      
      int state = bean.getServiceCurrentState();
      StatusItem.Status status;
      
      if (state == 0) {
        status = StatusItem.Status.OK;
      }
      else if (state == 1) {
        status = StatusItem.Status.WARNING;
      }
      else { // 2 (or something even weirder!)
        status = StatusItem.Status.ERROR;
      }
      
      item.setStatus(status);
      
      group.addItem(item);
      
    }
    
    return group;
  }
  
  private static String encode(String command) throws UnsupportedEncodingException {
    StringBuffer encoded = new StringBuffer();
    String[] tokens = command.split("/");
    for (String token : tokens) {
      String enc = URLEncoder.encode(token, "utf-8");
      encoded.append("/").append(enc);
    }
    
    return encoded.toString();
  }
  

}

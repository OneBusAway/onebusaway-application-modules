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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.onebusaway.enterprise.webapp.actions.status.model.IcingaItem;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusGroup;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusItem;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean.AffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class StatusProviderImpl implements StatusProvider {

  private static Logger _log = LoggerFactory.getLogger(StatusProviderImpl.class);
  
  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private IcingaItemPersistence _icingaItemPersistence;

  @Autowired
  private ConfigurationService _config;
    
  @Override
  public StatusGroup getAgencyServiceAlertStatus() {

    StatusGroup group = new StatusGroup();
    group.setTitle("Agency Advisories");
    group.setScope("Schedule and real-time availability at the agency level");
    group.setSource("Sound Transit administrators -- manual entry");

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
      List<ServiceAlertBean> beans = filterByTime(alerts.getList(), SystemTime.currentTimeMillis());
      for (ServiceAlertBean bean : beans) {
        StatusItem item = new StatusItem();
        item.setDescription(bean.getDescriptions().get(0).getValue());
        item.setTitle(agencyName + ": " + bean.getSummaries().get(0).getValue());
        item.setStatus(StatusItem.Status.ALERT);
        group.addItem(item);
      }
    }
    
    return group;
  }

  @Override
  public StatusGroup getIcingaStatus() {
    
    StatusGroup group = new StatusGroup();
    group.setTitle("System Monitoring");
    group.setScope("Monitoring and Infrastructure notices");
    group.setSource("Monitoring subsystem -- automated notifications");

    List<IcingaItem> results = _icingaItemPersistence.getIcingaItems();

    if (results == null || results.size() == 0) {
      _log.info("could not find icinga items in db");
      return group;
    }

    for (IcingaItem bean : results) {
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
  
  @Override
  public StatusGroup getAgencyMetadataStatus() {
    
    StatusGroup group = new StatusGroup();
    group.setTitle("General Notices");
    group.setScope("Informational updates about holiday and schedule changes");
    group.setSource("Agency Providers -- manual entry");

    AgencyMetadata[] response = new AgencyMetadata[0];
    
    String api =  _config.getConfigurationValueAsString("status.obaApi", 
        "http://localhost:8080/onebusaway-api-webapp/");
    String endpoint = _config.getConfigurationValueAsString("status.obaApiAgencyMetadata", 
        "api/where/agency-metadata/list.json");
    String apikey = _config.getConfigurationValueAsString("display.obaApiKey", "OBAKEY");    
    
    String url = api + endpoint + "?key=" + apikey;
    
    try {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(url);
      client.executeMethod(method);
      InputStream result = method.getResponseBodyAsStream();
      ObjectMapper mapper = new ObjectMapper();
      JsonNode tree = mapper.readTree(result);
      JsonNode value = tree.findValue("data");
      response = mapper.readValue(value.asText(), AgencyMetadata[].class);
    } catch (IOException e) {
      _log.error("Exception getting AgencyMetadata" + e);
      group.addItem(exceptionStatus(e));
      return group;
    }
    
    if (response == null) {
      group.addItem(exceptionStatus());
      return group;
    }
    
    for (AgencyMetadata bean : response) {
      if (!StringUtils.isEmpty(bean.getAgencyMessage())) {
        StatusItem item = new StatusItem();
        item.setTitle(bean.getName() + ": " + bean.getAgencyMessage());
        item.setStatus(StatusItem.Status.INFO);
        group.addItem(item);
      }
    }
    
    return group;
  }
  
  private static StatusItem exceptionStatus(Exception e) {
    StatusItem item = new StatusItem();
    item.setStatus(StatusItem.Status.WARNING);
    item.setTitle("Unable to load status group.");
    if (e != null) {
      item.setDescription(e.toString());
    }
    return item;
  }
  
  private static StatusItem exceptionStatus() {
    return exceptionStatus(null);
  }

  private List<ServiceAlertBean> filterByTime(List<ServiceAlertBean> beans, long time) {
    List<ServiceAlertBean> results = new ArrayList<ServiceAlertBean>();
    if (beans == null) return results;
    for (ServiceAlertBean bean : beans) {
      if (filterByTime(bean, time))
        results.add(bean);
    }
    return results;
  }

  private boolean filterByTime(ServiceAlertBean serviceAlert, long time) {
    if (time == -1
            || serviceAlert == null
            || serviceAlert.getPublicationWindows() == null
            || serviceAlert.getPublicationWindows().size() == 0)
      return true;
    for (TimeRangeBean publicationWindow : serviceAlert.getPublicationWindows()) {
      if ((publicationWindow.getFrom() <= time)
              && (publicationWindow.getTo() >= time)) {
        return true;
      }
    }
    return false;
  }

}

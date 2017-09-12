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

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.*;
import org.onebusaway.transit_data_federation.impl.service_alerts.*;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class ServiceAlertsBeanServiceImpl implements ServiceAlertsBeanService {

  private ServiceAlertsService _serviceAlertsService;

  @Autowired
  public void setServiceAlertsService(ServiceAlertsService serviceAlertsService) {
    _serviceAlertsService = serviceAlertsService;
  }

  @Override
  public ServiceAlertBean createServiceAlert(String agencyId,
      ServiceAlertBean situationBean) {
    ServiceAlertRecord serviceAlertRecord = getServiceAlertRecordFromServiceAlertBean(
        situationBean, agencyId);
    serviceAlertRecord = _serviceAlertsService.createOrUpdateServiceAlert(
        serviceAlertRecord);
    return getServiceAlertAsBean(serviceAlertRecord);
  }

  @Override
  public void updateServiceAlert(ServiceAlertBean situationBean) {
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(situationBean.getId());
    ServiceAlertRecord serviceAlertRecord = getServiceAlertRecordFromServiceAlertBean(
        situationBean, id.getAgencyId());
    
    ServiceAlertRecord dbServiceAlertRecord = _serviceAlertsService.getServiceAlertForId(id);
    
    if(dbServiceAlertRecord != null){
  	  serviceAlertRecord.setCopy(dbServiceAlertRecord.isCopy());
    }
    
    _serviceAlertsService.createOrUpdateServiceAlert(serviceAlertRecord);
  }
  
  @Override
  public ServiceAlertBean copyServiceAlert(String agencyId,
	      ServiceAlertBean situationBeanToCopy) {
	  situationBeanToCopy.setId(null);
	  ServiceAlertRecord serviceAlertRecordCopy = getServiceAlertRecordFromServiceAlertBean(
			  situationBeanToCopy, agencyId);
	  serviceAlertRecordCopy = _serviceAlertsService.copyServiceAlert(serviceAlertRecordCopy);
	  return getServiceAlertAsBean(serviceAlertRecordCopy);  
  }

  @Override
  public void removeServiceAlert(AgencyAndId situationId) {
    _serviceAlertsService.removeServiceAlert(situationId);
  }

  @Override
  public ServiceAlertBean getServiceAlertForId(AgencyAndId situationId) {
    ServiceAlertRecord serviceAlert = _serviceAlertsService.getServiceAlertForId(situationId);
    if (serviceAlert == null)
      return null;
    return getServiceAlertAsBean(serviceAlert);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForFederatedAgencyId(
      String agencyId) {
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlertsForFederatedAgencyId(agencyId);
    return list(serviceAlerts);
  }
  
  @Override
  public List<ServiceAlertRecordBean> getServiceAlertRecordsForFederatedAgencyId(
      String agencyId) {
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlertsForFederatedAgencyId(agencyId);
    return listRecordBeans(serviceAlerts);
  }

  @Override
  public void removeAllServiceAlertsForFederatedAgencyId(String agencyId) {
    _serviceAlertsService.removeAllServiceAlertsForFederatedAgencyId(agencyId);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForStopId(long time,
      AgencyAndId stopId) {
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlertsForStopId(
        time, stopId);
    return list(serviceAlerts);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId) {

    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlertsForStopCall(
        time, blockInstance, blockStopTime, vehicleId);

    return list(serviceAlerts);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForVehicleJourney(long time,
      BlockTripInstance blockTripInstance, AgencyAndId vehicleId) {

    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlertsForVehicleJourney(
        time, blockTripInstance, vehicleId);

    return list(serviceAlerts);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlerts(SituationQueryBean query) {
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlerts(query);
    return list(serviceAlerts);
  }

  /****
   * Private Methods
   ****/

  private List<ServiceAlertBean> list(List<ServiceAlertRecord> serviceAlerts) {
    List<ServiceAlertBean> beans = new ArrayList<ServiceAlertBean>();
    for (ServiceAlertRecord serviceAlert : serviceAlerts)
      beans.add(getServiceAlertAsBean(serviceAlert));
    return beans;
  }

  private ServiceAlertBean getServiceAlertAsBean(ServiceAlertRecord serviceAlert) {
	
    ServiceAlertBean bean = new ServiceAlertBean();

    AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getAgencyId(), serviceAlert.getServiceAlertId());
    bean.setId(AgencyAndIdLibrary.convertToString(id));
    bean.setCreationTime(serviceAlert.getCreationTime());

    bean.setActiveWindows(getRangesAsBeans(serviceAlert.getActiveWindows()));
    bean.setPublicationWindows(getRangesAsBeans(serviceAlert.getPublicationWindows()));

    /**
     * Reasons
     */
    if (serviceAlert.getCause() != null)
      bean.setReason(getCauseAsReason(serviceAlert.getCause()));

    /**
     * Text descriptions
     */
    bean.setSummaries(getTranslatedStringsAsNLSBeans(serviceAlert.getSummaries()));
    bean.setDescriptions(getTranslatedStringsAsNLSBeans(serviceAlert.getDescriptions()));
    bean.setUrls(getTranslatedStringsAsNLSBeans(serviceAlert.getUrls()));

    if (serviceAlert.getSeverity() != null)
      bean.setSeverity(serviceAlert.getSeverity());

    bean.setAllAffects(getAffectsAsBeans(serviceAlert));
    bean.setConsequences(getConsequencesAsBeans(serviceAlert));
    bean.setSource(serviceAlert.getSource());
    
    return bean;
  }
  
  private List<ServiceAlertRecordBean> listRecordBeans(List<ServiceAlertRecord> serviceAlerts) {
    List<ServiceAlertRecordBean> beans = new ArrayList<ServiceAlertRecordBean>();
    for (ServiceAlertRecord serviceAlert : serviceAlerts)
      beans.add(getServiceAlertAsRecordBean(serviceAlert));
    return beans;
  }
  
  private ServiceAlertRecordBean getServiceAlertAsRecordBean(ServiceAlertRecord serviceAlert) {
	ServiceAlertBean bean = getServiceAlertAsBean(serviceAlert);
	ServiceAlertRecordBean serviceAlertRecordBean = new ServiceAlertRecordBean();
    serviceAlertRecordBean.setServiceAlertBean(bean);
    serviceAlertRecordBean.setCopy(serviceAlert.isCopy());
    
    return serviceAlertRecordBean;
  }

  private ServiceAlertRecord getServiceAlertRecordFromServiceAlertBean(
      ServiceAlertBean bean, String agencyId) {
	  
    ServiceAlertRecord serviceAlertRecord = new ServiceAlertRecord();
    serviceAlertRecord.setAgencyId(agencyId);
    if (bean.getId() != null && !bean.getId().isEmpty()) {
      AgencyAndId id;
      if(bean.getId().indexOf("_") > -1){
        id = AgencyAndIdLibrary.convertFromString(bean.getId());
      }else{
        id = AgencyAndIdLibrary.convertFromString(agencyId + "_" + bean.getId());
      }
      serviceAlertRecord.setServiceAlertId(id.getId());
      serviceAlertRecord.setAgencyId(id.getAgencyId());
    }
    serviceAlertRecord.setCreationTime(bean.getCreationTime());
    serviceAlertRecord.setActiveWindows(getBeansAsRanges(bean.getActiveWindows()));
    serviceAlertRecord.setPublicationWindows(getBeansAsRanges(bean.getPublicationWindows()));

    /**
     * Reasons
     */
    serviceAlertRecord.setCause(getReasonAsCause(bean.getReason()));

    /**
     * Text descriptions
     */
    serviceAlertRecord.setSummaries(new HashSet<ServiceAlertLocalizedString>());
    if(bean.getSummaries() != null){
        for(NaturalLanguageStringBean summary : bean.getSummaries()){
            ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
            string.setLanguage(summary.getLang());
            string.setValue(summary.getValue());
            string.setServiceAlertRecord(serviceAlertRecord);
            serviceAlertRecord.getSummaries().add(string);
        }
    }

    serviceAlertRecord.setDescriptions(new HashSet<ServiceAlertLocalizedString>());
      if(bean.getDescriptions() != null){
          for(NaturalLanguageStringBean summary : bean.getDescriptions()){
              ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
              string.setLanguage(summary.getLang());
              string.setValue(summary.getValue());
              string.setServiceAlertRecord(serviceAlertRecord);
              serviceAlertRecord.getDescriptions().add(string);
          }
      }

    serviceAlertRecord.setUrls(new HashSet<ServiceAlertLocalizedString>());
      if(bean.getUrls() != null){
          for(NaturalLanguageStringBean url : bean.getUrls()){
              ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
              string.setLanguage(url.getLang());
              string.setValue(url.getValue());
              string.setServiceAlertRecord(serviceAlertRecord);
              serviceAlertRecord.getUrls().add(string);
          }
      }

    if (bean.getSeverity() != null)
      serviceAlertRecord.setSeverity(bean.getSeverity());

    serviceAlertRecord.setAllAffects(getBeanAsAffects(bean));
    for(ServiceAlertsSituationAffectsClause clause : serviceAlertRecord.getAllAffects()){
        clause.setServiceAlertRecord(serviceAlertRecord);
    }

    serviceAlertRecord.setConsequences(getBeanAsConsequences(bean));
    for(ServiceAlertSituationConsequenceClause clause : serviceAlertRecord.getConsequences()){
        clause.setServiceAlertRecord(serviceAlertRecord);
    }

    serviceAlertRecord.setSource(bean.getSource());

    return serviceAlertRecord;
  }

  /****
   * Situations Affects
   ****/

  private List<SituationAffectsBean> getAffectsAsBeans(ServiceAlertRecord serviceAlert) {

    if (serviceAlert.getAllAffects().size() == 0)
      return null;

    List<SituationAffectsBean> beans = new ArrayList<SituationAffectsBean>();

    for (ServiceAlertsSituationAffectsClause affects : serviceAlert.getAllAffects()) {
      SituationAffectsBean bean = new SituationAffectsBean();
      if (affects.getAgencyId() != null)
        bean.setAgencyId(affects.getAgencyId());
      if (affects.getApplicationId() != null)
        bean.setApplicationId(affects.getApplicationId());
      if (affects.getRouteId() != null) {
        bean.setRouteId(affects.getRouteId());
      }
      if (affects.getDirectionId() != null)
        bean.setDirectionId(affects.getDirectionId());
      if (affects.getTripId() != null) {
        bean.setTripId(affects.getTripId());
      }
      if (affects.getStopId() != null) {
        bean.setStopId(affects.getStopId());
      }
      if (affects.getApplicationId()  != null)
        bean.setApplicationId(affects.getApplicationId());
      beans.add(bean);
    }
    return beans;
  }

  private Set<ServiceAlertsSituationAffectsClause> getBeanAsAffects(ServiceAlertBean bean) {

    Set<ServiceAlertsSituationAffectsClause> affectsList = new HashSet<ServiceAlertsSituationAffectsClause>();

    if (!CollectionsLibrary.isEmpty(bean.getAllAffects())) {
      for (SituationAffectsBean affectsBean : bean.getAllAffects()) {
        ServiceAlertsSituationAffectsClause affects = new ServiceAlertsSituationAffectsClause();
        if (affectsBean.getAgencyId() != null)
          affects.setAgencyId(affectsBean.getAgencyId());
        if (affectsBean.getApplicationId() != null)
          affects.setApplicationId(affectsBean.getApplicationId());
        if (affectsBean.getRouteId() != null) {
          affects.setRouteId(affectsBean.getRouteId());
        }
        if (affectsBean.getDirectionId() != null)
          affects.setDirectionId(affectsBean.getDirectionId());
        if (affectsBean.getTripId() != null) {
          affects.setTripId(affectsBean.getTripId());
        }
        if (affectsBean.getStopId() != null) {
          affects.setStopId(affectsBean.getStopId());
        }
        affectsList.add(affects);
      }
    }

    return affectsList;
  }

  /****
   * Consequence
   ****/

  private List<SituationConsequenceBean> getConsequencesAsBeans(
      ServiceAlertRecord serviceAlert) {
    if (serviceAlert.getConsequences().size() == 0)
      return null;
    List<SituationConsequenceBean> beans = new ArrayList<SituationConsequenceBean>();
    for (ServiceAlertSituationConsequenceClause consequence : serviceAlert.getConsequences()) {
      SituationConsequenceBean bean = new SituationConsequenceBean();
      if (consequence.getEffect() != null)
        bean.setEffect(consequence.getEffect());
      if (consequence.getDetourPath() != null)
        bean.setDetourPath(consequence.getDetourPath());
      if (consequence.getDetourStopIds().size() != 0) {
        List<String> stopIds = new ArrayList<String>();
        for (String stopId : consequence.getDetourStopIds()) {
          AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getAgencyId(), stopId);
          stopIds.add(AgencyAndId.convertToString(id));
        }
        bean.setDetourStopIds(stopIds);
      }
      beans.add(bean);
    }
    return beans;
  }

  private Set<ServiceAlertSituationConsequenceClause> getBeanAsConsequences(ServiceAlertBean bean) {

    Set<ServiceAlertSituationConsequenceClause> consequences = new HashSet<ServiceAlertSituationConsequenceClause>();

    if (!CollectionsLibrary.isEmpty(bean.getConsequences())) {
      for (SituationConsequenceBean consequence : bean.getConsequences()) {
        ServiceAlertSituationConsequenceClause consequenceClause = new ServiceAlertSituationConsequenceClause();
        if (consequence.getEffect() != null)
          consequenceClause.setEffect(consequence.getEffect());
        if (consequence.getDetourPath() != null)
          consequenceClause.setDetourPath(consequence.getDetourPath());
        if (!CollectionsLibrary.isEmpty(consequence.getDetourStopIds())) {
          Set<String> detourStopIds = new HashSet<String>();
          for (String detourStopId : consequence.getDetourStopIds()) {
            detourStopIds.add(detourStopId);
          }
          consequenceClause.setDetourStopIds(detourStopIds);
        }
        consequences.add(consequenceClause);
      }
    }

    return consequences;
  }

  /****
   * 
   ****/

  private ECause getReasonAsCause(String reason) {
    if (reason == null)
      return ECause.UNKNOWN_CAUSE;
    return ECause.valueOf(reason);
  }

  private String getCauseAsReason(ECause cause) {
    return cause.toString();
  }

  /****
   * 
   ****/

  private List<TimeRangeBean> getRangesAsBeans(Set<ServiceAlertTimeRange> ranges) {
    if (ranges == null || ranges.isEmpty())
      return null;
    List<TimeRangeBean> beans = new ArrayList<TimeRangeBean>();
    for (ServiceAlertTimeRange range : ranges) {
      TimeRangeBean bean = new TimeRangeBean();
      if (range.getFromValue() != null)
        bean.setFrom(range.getFromValue());
      if (range.getToValue() != null)
        bean.setTo(range.getToValue());
      beans.add(bean);
    }
    return beans;
  }

  private Set<ServiceAlertTimeRange> getBeansAsRanges(List<TimeRangeBean> beans) {
    if (beans == null)
      return Collections.emptySet();
      Set<ServiceAlertTimeRange> ranges = new HashSet<ServiceAlertTimeRange>();
    for (TimeRangeBean bean : beans) {
      ServiceAlertTimeRange range = new ServiceAlertTimeRange();
      if (bean.getFrom() > 0)
        range.setFromValue(bean.getFrom());
      if (bean.getTo() > 0)
        range.setToValue(bean.getTo());
      if (range.getFromValue() != null || range.getToValue() != null)
        ranges.add(range);
    }
    return ranges;
  }

  private List<NaturalLanguageStringBean> getTranslatedStringsAsNLSBeans(
      Set<ServiceAlertLocalizedString> strings) {

    if (strings == null || strings.size() == 0)
      return null;

    List<NaturalLanguageStringBean> nlsBeans = new ArrayList<NaturalLanguageStringBean>();
    for (ServiceAlertLocalizedString translation : strings) {
      NaturalLanguageStringBean nls = new NaturalLanguageStringBean();
      nls.setValue(translation.getValue());
      nls.setLang(translation.getLanguage());
      nlsBeans.add(nls);
    }

    return nlsBeans;
  }

}

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
package org.onebusaway.api.actions.siri.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.api.actions.siri.service.RealtimeServiceV2;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.DateUtil;
import org.onebusaway.siri.OneBusAwayConsequence;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.service_alerts.EEffect;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;

import uk.org.siri.siri_2.AffectedOperatorStructure;
import uk.org.siri.siri_2.AffectedVehicleJourneyStructure;
import uk.org.siri.siri_2.AffectsScopeStructure;
import uk.org.siri.siri_2.AffectsScopeStructure.Operators;
import uk.org.siri.siri_2.AffectsScopeStructure.VehicleJourneys;
import uk.org.siri.siri_2.DefaultedTextStructure;
import uk.org.siri.siri_2.DirectionRefStructure;
import uk.org.siri.siri_2.EntryQualifierStructure;
import uk.org.siri.siri_2.ExtensionsStructure;
import uk.org.siri.siri_2.HalfOpenTimestampOutputRangeStructure;
import uk.org.siri.siri_2.LineRefStructure;
import uk.org.siri.siri_2.MonitoredStopVisitStructure;
import uk.org.siri.siri_2.PtConsequenceStructure;
import uk.org.siri.siri_2.PtConsequencesStructure;
import uk.org.siri.siri_2.PtSituationElementStructure;
import uk.org.siri.siri_2.ServiceConditionEnumeration;
import uk.org.siri.siri_2.ServiceDelivery;
import uk.org.siri.siri_2.SeverityEnumeration;
import uk.org.siri.siri_2.SituationExchangeDeliveryStructure;
import uk.org.siri.siri_2.SituationExchangeDeliveryStructure.Situations;
import uk.org.siri.siri_2.SituationRefStructure;
import uk.org.siri.siri_2.VehicleActivityStructure;

public class ServiceAlertsHelperV2 {

  public void addSituationExchangeToSiriForStops(
      ServiceDelivery serviceDelivery,
      List<MonitoredStopVisitStructure> visits,
      TransitDataService transitDataService, List<AgencyAndId> stopIds) {

    Map<String, PtSituationElementStructure> ptSituationElements = new HashMap<String, PtSituationElementStructure>();

    for (MonitoredStopVisitStructure visit : visits) {
      if (visit.getMonitoredVehicleJourney() != null)
        addSituationElement(transitDataService, ptSituationElements,
            visit.getMonitoredVehicleJourney().getSituationRef());
    }

    if (stopIds != null && stopIds.size() > 0) {
      for (AgencyAndId stopId : stopIds) {
        String stopIdString = stopId.toString();

        // First get service alerts for the stop
        SituationQueryBean query = new SituationQueryBean();
        List<String> stopIdStrings = new ArrayList<String>();
        stopIdStrings.add(stopIdString);
        SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
        query.getAffects().add(affects);
        affects.setStopId(stopIdString);

        addFromQuery(transitDataService, ptSituationElements, query);

        // Now also add service alerts for (route+direction)s of the stop
        query = new SituationQueryBean();
        
        StopBean stopBean = transitDataService.getStop(stopIdString);
        List<RouteBean> routes = stopBean.getRoutes();
        for (RouteBean route : routes) {
          StopsForRouteBean stopsForRoute = transitDataService.getStopsForRoute(route.getId());
          List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
          for (StopGroupingBean stopGrouping : stopGroupings) {
            if (!stopGrouping.getType().equalsIgnoreCase("direction"))
              continue;
            for (StopGroupBean stopGroup : stopGrouping.getStopGroups()) {
              handleStopGroupBean(stopIdString, query, route, stopGroup);
            }
          }
        }

        addFromQuery(transitDataService, ptSituationElements, query);
      }
    }

    addPtSituationElementsToServiceDelivery(serviceDelivery,
        ptSituationElements);
  }

  private void handleStopGroupBean(String stopIdString,
      SituationQueryBean query, RouteBean route, StopGroupBean stopGroup) {

    List<StopGroupBean> subGroups = stopGroup.getSubGroups();
    if (subGroups != null && !subGroups.isEmpty()) {
      for (StopGroupBean stopSubGroup : subGroups) {
        handleStopGroupBean(stopIdString, query, route, stopSubGroup);
      }
    }
    
    String direction = stopGroup.getId();
    for (String groupStopId : stopGroup.getStopIds()) {
      if (groupStopId.equals(stopIdString)) {
      SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
      query.getAffects().add(affects);
      affects.setRouteId(route.getId());
      affects.setDirectionId(direction);
      }
    }
  }

  private void addFromQuery(TransitDataService transitDataService,
      Map<String, PtSituationElementStructure> ptSituationElements,
      SituationQueryBean queryBean) {
    ListBean<ServiceAlertBean> serviceAlerts = transitDataService.getServiceAlerts(queryBean);
    ServiceAlertsHelperV2 helper = new ServiceAlertsHelperV2();
    for (ServiceAlertBean bean : serviceAlerts.getList()) {
      PtSituationElementStructure ptSit = helper.getServiceAlertBeanAsPtSituationElementStructure(bean);
      ptSituationElements.put(ptSit.getSituationNumber().getValue(), ptSit);
    }
  }

  public void addSituationExchangeToServiceDelivery(ServiceDelivery serviceDelivery,
      Collection<ServiceAlertBean> serviceAlerts) {
    Situations situations = new Situations();
    for (ServiceAlertBean serviceAlert : serviceAlerts) {
      situations.getPtSituationElement().add(
          getServiceAlertBeanAsPtSituationElementStructure(serviceAlert));
    }

    if (situations.getPtSituationElement().size() > 0) {
      SituationExchangeDeliveryStructure situationExchangeDelivery = new SituationExchangeDeliveryStructure();
      situationExchangeDelivery.setSituations(situations);
      serviceDelivery.getSituationExchangeDelivery().add(
          situationExchangeDelivery);
    }
  }
/*
  public void addSituationExchangeToServiceDelivery(ServiceDelivery serviceDelivery,
      List<VehicleActivityStructure> activities,
      TransitDataService transitDataService) {
    
    addSituationExchangeToServiceDelivery(serviceDelivery, activities, transitDataService, null);
  }
  */
  public void addSituationExchangeToServiceDelivery(ServiceDelivery serviceDelivery,
      List<VehicleActivityStructure> activities,
      TransitDataService transitDataService, List<AgencyAndId> routeIds) {

    if (activities == null)
      return;
    Map<String, PtSituationElementStructure> ptSituationElements = new HashMap<String, PtSituationElementStructure>();
    for (VehicleActivityStructure activity : activities) {
      if (activity.getMonitoredVehicleJourney() != null) {
        addSituationElement(transitDataService, ptSituationElements,
            activity.getMonitoredVehicleJourney().getSituationRef());
      }
    }
    addPtSituationElementsToServiceDelivery(serviceDelivery,
        ptSituationElements);
    
    if (routeIds == null)
      return;
    
    List<ServiceAlertBean> serviceAlerts = new ArrayList<ServiceAlertBean>();
    
    for (AgencyAndId routeId :  routeIds) {
      SituationQueryBean query = new SituationQueryBean();
      SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
      query.getAffects().add(affects);
      affects.setRouteId(routeId.toString());
      ListBean<ServiceAlertBean> serviceAlertsForRoute = transitDataService.getServiceAlerts(query);
      // now query route + stop combinations
      StopsForRouteBean stopsForRoute = transitDataService.getStopsForRoute(routeId.toString());
      if (stopsForRoute != null) {
        for (StopBean stopBean : stopsForRoute.getStops()) {
          SituationQueryBean stopRouteQuery = new SituationQueryBean();
          SituationQueryBean.AffectsBean stopRouteAffects = new SituationQueryBean.AffectsBean();
          stopRouteQuery.getAffects().add(stopRouteAffects);
          stopRouteAffects.setRouteId(routeId.toString());
          stopRouteAffects.setStopId(stopBean.getId());
          ListBean<ServiceAlertBean> serviceAlertsForRouteStop = transitDataService.getServiceAlerts(stopRouteQuery);
          if (serviceAlertsForRouteStop != null)
            serviceAlerts.addAll(serviceAlertsForRouteStop.getList());
        }
      }
      if (serviceAlertsForRoute != null) {
        serviceAlerts.addAll(serviceAlertsForRoute.getList());
      }
    }
    
    if (serviceAlerts.size() == 0)
      return;
    
    addSituationExchangeToServiceDelivery(serviceDelivery, serviceAlerts);
    
  }
/*
  public void addSituationExchangeToServiceDelivery(ServiceDelivery serviceDelivery,
      Map<String, ServiceAlertBean> currentServiceAlerts) {
    addSituationExchangeToServiceDelivery(serviceDelivery, currentServiceAlerts.values());
  }

  public void addClosedSituationExchangesToSiri(
      ServiceDelivery serviceDelivery, Collection<String> deletedIds) {
    Map<String, PtSituationElementStructure> ptSituationElements = new HashMap<String, PtSituationElementStructure>();

    for (String id : deletedIds) {
      PtSituationElementStructure ptSit = new PtSituationElementStructure();
      EntryQualifierStructure value = new EntryQualifierStructure();
      value.setValue(id);
      ptSit.setSituationNumber(value);
      ptSit.setProgress(WorkflowStatusEnumeration.CLOSED);
      ptSituationElements.put(id, ptSit);
    }

    addPtSituationElementsToServiceDelivery(serviceDelivery,
        ptSituationElements);

  }
*/
  private void addSituationElement(TransitDataService transitDataService,
      Map<String, PtSituationElementStructure> ptSituationElements,
      List<SituationRefStructure> situationRefs) {
    if (situationRefs == null)
      return;
    for (SituationRefStructure situationRef : situationRefs) {
      String situationId = situationRef.getSituationSimpleRef().getValue();
      ServiceAlertBean serviceAlert = transitDataService.getServiceAlertForId(situationId);
      PtSituationElementStructure e = getServiceAlertBeanAsPtSituationElementStructure(serviceAlert);
      ptSituationElements.put(situationId, e);
    }
  }

  private void addPtSituationElementsToServiceDelivery(
      ServiceDelivery serviceDelivery,
      Map<String, PtSituationElementStructure> ptSituationElements) {
    if (serviceDelivery == null || ptSituationElements == null)
      return;
    
    SituationExchangeDeliveryStructure situationExchangeDeliveryStructure;
    // Check if the serviceDelivery already has a situationDeliveryStructure in its list
    if (serviceDelivery.getSituationExchangeDelivery().size() > 0) {
      // It does, so use it
      situationExchangeDeliveryStructure = serviceDelivery.getSituationExchangeDelivery().get(0);
    } else {
      // It does not, so create a new one and use it
      situationExchangeDeliveryStructure = new SituationExchangeDeliveryStructure();
    }
    
    // Try to get the situation object from our situationExchangeDeliveryStructure
    Situations situations = situationExchangeDeliveryStructure.getSituations();
    // If it contained no situation object, create a new one and add it to our situationExchangeDeliveryStructure
    if (situations == null) {
      situations = new Situations();
      situationExchangeDeliveryStructure.setSituations(situations);
    }
    
    // Iterate through our ptSituationElements and add them to our situations object
    for (PtSituationElementStructure p : ptSituationElements.values()) {
      situations.getPtSituationElement().add(p);
    }

    // If our situationExchangeDeliveryStructure has a situations object...
    if (situationExchangeDeliveryStructure.getSituations() != null
        // AND our situations object's ptSituationsElement is not empty
        && (situationExchangeDeliveryStructure.getSituations().getPtSituationElement() != null
            && !situationExchangeDeliveryStructure.getSituations().getPtSituationElement().isEmpty())
        // AND our serviceDelivery doesn't already contain our situationExchangeDeliveryStructure
        && !serviceDelivery.getSituationExchangeDelivery().contains(situationExchangeDeliveryStructure)) {
      
      // Add our situationExchangeDeliveryStructure to our serviceDelivery
      serviceDelivery.getSituationExchangeDelivery().add(situationExchangeDeliveryStructure);
    }
  }

  public void addGlobalServiceAlertsToServiceDelivery(ServiceDelivery serviceDelivery, RealtimeServiceV2 realtimeService) {
    List<ServiceAlertBean> serviceAlertBeans = realtimeService.getServiceAlertsGlobal();
    if (serviceAlertBeans == null) return;
    Map<String, PtSituationElementStructure> ptSituationElements = new HashMap<String, PtSituationElementStructure>();
    for (ServiceAlertBean serviceAlertBean : serviceAlertBeans) {
      ptSituationElements.put(serviceAlertBean.getId(), getServiceAlertBeanAsPtSituationElementStructure(serviceAlertBean));
    }
    addPtSituationElementsToServiceDelivery(serviceDelivery, ptSituationElements);
  }

  public PtSituationElementStructure getServiceAlertBeanAsPtSituationElementStructure(
      ServiceAlertBean serviceAlert) {
    PtSituationElementStructure ptSit = new PtSituationElementStructure();

    EntryQualifierStructure value = new EntryQualifierStructure();
    value.setValue(serviceAlert.getId());
    ptSit.setSituationNumber(value);

    if (serviceAlert.getCreationTime() != 0)
      ptSit.setCreationTime(DateUtil.toXmlGregorianCalendar(serviceAlert.getCreationTime()));
    
    handleDescriptions(serviceAlert, ptSit);
    handleOtherFields(serviceAlert, ptSit);
    handleAffects(serviceAlert, ptSit);
    handleConsequences(serviceAlert, ptSit);

    return ptSit;
  }

  private void handleDescriptions(ServiceAlertBean serviceAlert,
      PtSituationElementStructure ptSituation) {

    if (serviceAlert == null)
      return;
    if (serviceAlert.getSummaries() != null)
      for (NaturalLanguageStringBean summary : serviceAlert.getSummaries()) {
        ptSituation.getSummary().add(createDefaultedTextStructure(summary));
      }
    if (serviceAlert.getDescriptions() != null)
      for (NaturalLanguageStringBean description : serviceAlert.getDescriptions()) {
        ptSituation.getDescription().add(createDefaultedTextStructure(description));
      }
  }

  private DefaultedTextStructure createDefaultedTextStructure(
      NaturalLanguageStringBean summary) {
    DefaultedTextStructure d = new DefaultedTextStructure();
    d.setLang(summary.getLang());
    d.setValue(summary.getValue());
    return d;
  }

  private DefaultedTextStructure createDefaultedTextStructure(
      String text) {
    DefaultedTextStructure d = new DefaultedTextStructure();
    d.setLang("EN");
    d.setValue(text);
    return d;
  }

  private void handleOtherFields(ServiceAlertBean serviceAlert,
      PtSituationElementStructure ptSituation) {

    if (serviceAlert == null || serviceAlert.getPublicationWindows() == null)
      return;
    // TODO Not handling severity yet.
    ptSituation.setSeverity(SeverityEnumeration.UNDEFINED);
    // TODO - LCARABALLO - Changed from HalfOpenTimestampRangeStructure to HalfOpenTimestampOutputRangeStructure
    HalfOpenTimestampOutputRangeStructure timestampRangeStructure = new HalfOpenTimestampOutputRangeStructure();
    if (serviceAlert.getPublicationWindows() != null && !serviceAlert.getPublicationWindows().isEmpty()) {
      timestampRangeStructure.setStartTime(DateUtil.toXmlGregorianCalendar(serviceAlert.getPublicationWindows().get(
          0).getFrom()));
      timestampRangeStructure.setEndTime(DateUtil.toXmlGregorianCalendar(serviceAlert.getPublicationWindows().get(
          0).getTo()));
      ptSituation.setPublicationWindow(timestampRangeStructure);
    }
  }
/*
  public Date serviceAlertTimeToDate(long time) {
    if (time == 0)
      return null;
    return new Date(time);
  }

  @SuppressWarnings("unused")
  private void handlReasons(PtSituationElementStructure ptSituation,
      ServiceAlertBean serviceAlert) {
    throw new RuntimeException("handleReasons not implemented");
  }
*/
  private void handleAffects(ServiceAlertBean serviceAlert,
      PtSituationElementStructure ptSituation) {

    if (serviceAlert.getAllAffects() == null)
      return;

    boolean hasOperators = false;

    AffectsScopeStructure affectsStructure = new AffectsScopeStructure();
    VehicleJourneys vehicleJourneys = new VehicleJourneys();
    for (SituationAffectsBean affects : serviceAlert.getAllAffects()) {
      String agencyId = affects.getAgencyId();
      if (agencyId != null) {
        Operators operators = new Operators();
        if (StringUtils.equals(agencyId, "__ALL_OPERATORS__")) {
          operators.setAllOperators("");
        } else {
          AffectedOperatorStructure affectedOperator = new AffectedOperatorStructure();
          affectedOperator.getOperatorName().add(createDefaultedTextStructure(agencyId));
          operators.getAffectedOperator().add(affectedOperator);
        }
        affectsStructure.setOperators(operators);
        hasOperators = true;
      }
      String routeId = affects.getRouteId(); // LineRef
      String directionId = affects.getDirectionId();
      if (!StringUtils.isBlank(routeId)) {
        AffectedVehicleJourneyStructure affectedVehicleJourneyStructure = new AffectedVehicleJourneyStructure();
        LineRefStructure lineRefStructure = new LineRefStructure();
        lineRefStructure.setValue(routeId);
        affectedVehicleJourneyStructure.setLineRef(lineRefStructure);
        if (!StringUtils.isBlank(directionId)) {
          DirectionRefStructure directionRefStructure = new DirectionRefStructure();
          directionRefStructure.setValue(directionId);
          affectedVehicleJourneyStructure.setDirectionRef(directionRefStructure);
        }
        vehicleJourneys.getAffectedVehicleJourney().add(
            affectedVehicleJourneyStructure);
      }
    }
    if (vehicleJourneys.getAffectedVehicleJourney().size() > 0) {
      affectsStructure.setVehicleJourneys(vehicleJourneys);
    }
    if ((vehicleJourneys.getAffectedVehicleJourney().size() > 0) || hasOperators) {
      ptSituation.setAffects(affectsStructure);
    }

  }

  private void handleConsequences(ServiceAlertBean serviceAlert,
      PtSituationElementStructure ptSituation) {

    if (serviceAlert == null)
      return;
    List<SituationConsequenceBean> consequences = serviceAlert.getConsequences();
    if (consequences == null || consequences.isEmpty())
      return;

    PtConsequencesStructure ptConsequences = new PtConsequencesStructure();
    ptSituation.setConsequences(ptConsequences);

    for (SituationConsequenceBean consequence : consequences) {
      EEffect effect = consequence.getEffect();
      PtConsequenceStructure ptConsequenceStructure = new PtConsequenceStructure();
      if (effect != null) {
        ServiceConditionEnumeration serviceCondition = getEFfectAsCondition(effect);
        ptConsequenceStructure.getCondition().add(serviceCondition);
      }

      String detourPath = consequence.getDetourPath();
      if (!StringUtils.isBlank(detourPath)) {
        ExtensionsStructure extensionStructure = new ExtensionsStructure();
        OneBusAwayConsequence oneBusAwayConsequence = new OneBusAwayConsequence();
        oneBusAwayConsequence.setDiversionPath(detourPath);
        extensionStructure.setAny(oneBusAwayConsequence);
        ptConsequenceStructure.setExtensions(extensionStructure);
      }

      ptConsequences.getConsequence().add(ptConsequenceStructure);
    }

  }
  
  private ServiceConditionEnumeration getEFfectAsCondition(EEffect effect) {
    switch (effect) {

      case NO_SERVICE:
        return ServiceConditionEnumeration.NO_SERVICE;

      case SIGNIFICANT_DELAYS:
        return ServiceConditionEnumeration.DELAYED;

      case DETOUR:
        return ServiceConditionEnumeration.DIVERTED;

      case ADDITIONAL_SERVICE:
        return ServiceConditionEnumeration.ADDITIONAL_SERVICE;

      case REDUCED_SERVICE:
        return ServiceConditionEnumeration.DISRUPTED;

      case MODIFIED_SERVICE:
        return ServiceConditionEnumeration.ALTERED;

      case OTHER_EFFECT:
        return ServiceConditionEnumeration.NORMAL_SERVICE;

      case UNKNOWN_EFFECT:
        return ServiceConditionEnumeration.UNKNOWN;

      default:
        return ServiceConditionEnumeration.UNKNOWN;
    }
  }
}

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

import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.*;
import org.onebusaway.alerts.impl.*;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.onebusaway.alerts.impl.ServiceAlertBeanHelper.*;

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
    return ServiceAlertBeanHelper.getServiceAlertAsBean(serviceAlertRecord);
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
	  return ServiceAlertBeanHelper.getServiceAlertAsBean(serviceAlertRecordCopy);
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
    return ServiceAlertBeanHelper.getServiceAlertAsBean(serviceAlert);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForFederatedAgencyId(
      String agencyId) {
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlertsForFederatedAgencyId(agencyId);
    return ServiceAlertBeanHelper.list(serviceAlerts);
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
    return ServiceAlertBeanHelper.list(serviceAlerts);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId) {
    Set<ServiceAlertRecord> alerts = new HashSet<>();
    BlockTripEntry blockTrip = blockStopTime.getTrip();
    TripEntry trip = blockTrip.getTrip();
    AgencyAndId tripId = trip.getId();
    AgencyAndId lineId = trip.getRouteCollection().getId();
    String directionId = trip.getDirectionId();
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    StopEntry stop = stopTime.getStop();
    AgencyAndId stopId = stop.getId();

    Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
    /*
     * TODO: Temporarily disable
     */
    /*
     * getServiceAlertIdsForKey(_serviceAlertsIdsByAgencyId,
     * lineId.getAgencyId(), serviceAlertIds);
     */

    alerts.addAll(_serviceAlertsService.getServiceAlertsForRouteId(time, lineId));
    alerts.addAll(_serviceAlertsService.getServiceAlertsForRouteAndStopId(time, lineId, stopId));
    /**
     * Remember that direction is optional
     */
    if (directionId != null) {
      alerts.addAll(_serviceAlertsService.getServiceAlertsForRouteAndDirection(time, lineId, stopId, directionId));
    }

    alerts.addAll(_serviceAlertsService.getServiceAlertsForTripAndStopId(time, tripId, stopId));

    return ServiceAlertBeanHelper.list(new ArrayList<>(alerts));
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForVehicleJourney(long time,
      BlockTripInstance blockTripInstance, AgencyAndId vehicleId) {

    Set<ServiceAlertRecord> alerts = new HashSet<>();
    BlockTripEntry blockTrip = blockTripInstance.getBlockTrip();
    TripEntry trip = blockTrip.getTrip();
    AgencyAndId lineId = trip.getRouteCollection().getId();
    alerts.addAll(_serviceAlertsService.getServiceAlertsForRouteAndDirection(time, lineId, trip.getId(), trip.getDirectionId()));
    return ServiceAlertBeanHelper.list(new ArrayList<>(alerts));
  }

  @Override
  public List<ServiceAlertBean> getServiceAlerts(SituationQueryBean query) {
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getServiceAlerts(query);
    return ServiceAlertBeanHelper.list(serviceAlerts);
  }

}

/**
 * Copyright (C) 2010 OpenPlans
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
package org.onebusaway.presentation.impl.realtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.realtime.SiriSupport.OnwardCallsMode;
import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.onebusaway.transit_data_federation.siri.SiriJsonSerializer;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializer;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;

/**
 * A source of SIRI classes containing real time data, subject to the conventions expressed
 * in the PresentationService.
 * 
 * @author jmaki
 *
 */
@Component
public class RealtimeServiceImpl implements RealtimeService {

  private TransitDataService _transitDataService;

  private ConfigurationService _configurationService;

  private PresentationService _presentationService;
  
  private SiriXmlSerializer _siriXmlSerializer = new SiriXmlSerializer();

  private SiriJsonSerializer _siriJsonSerializer = new SiriJsonSerializer();
  
  private static Logger _log = LoggerFactory.getLogger(PresentationServiceImpl.class);
  
  private static final long MILLISECONDS_IN_YEAR = 1000L * 60 * 60 * 24 * 365;

  private Long _now = null;

  private Boolean _useApc = null;
  
  @Override
  public void setTime(long time) {
    _now = time;
    _presentationService.setTime(time);
  }

  public long getTime() {
    if(_now != null)
      return _now;
    else
      return SystemTime.currentTimeMillis();
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setConfigurationService(ConfigurationService configService) {
    _configurationService = configService;
  }

  @Autowired
  public void setPresentationService(PresentationService presentationService) {
    _presentationService = presentationService;
  }
  
  @Override
  public PresentationService getPresentationService() {
    return _presentationService;
  }

  @Override
  public SiriJsonSerializer getSiriJsonSerializer() {
    return _siriJsonSerializer;
  }
  
  @Override
  public SiriXmlSerializer getSiriXmlSerializer() {
    return _siriXmlSerializer;
  }

  /**
   * SIRI METHODS
   */

  @Override
  public List<VehicleActivityStructure> getVehicleActivityForRoute(String routeId, String directionId, int maximumOnwardCalls, long currentTime, boolean showRawLocation) {
    List<VehicleActivityStructure> output = new ArrayList<VehicleActivityStructure>();
        
    ListBean<TripDetailsBean> trips = getAllTripsForRoute(routeId, currentTime);
    for(TripDetailsBean tripDetails : trips.getList()) {
      if(tripDetails == null)
        continue;
      
      // filter out interlined routes
      if(routeId != null && !tripDetails.getTrip().getRoute().getId().equals(routeId))
          continue;

      // filtered out by user
      if (tripDetails.getTrip().getDirectionId() != null) {
        if(directionId != null && !tripDetails.getTrip().getDirectionId().equals(directionId)) {
          continue;
        }
      }
      
      if(!_presentationService.include(tripDetails.getStatus()))
          continue;
      
      VehicleActivityStructure activity = new VehicleActivityStructure();
      // Check for Realtime Data
      if(!tripDetails.getStatus().isPredicted()){
    	  activity.setRecordedAtTime(new Date(getTime()));
      }
      else{
    	  activity.setRecordedAtTime(new Date(tripDetails.getStatus().getLastUpdateTime()));
      }
      
      List<TimepointPredictionRecord> timePredictionRecords = null;
      
	  timePredictionRecords = _transitDataService.getPredictionRecordsForTrip(AgencyAndId.convertFromString(routeId).getAgencyId(), tripDetails.getStatus());

	  boolean showApc = useApc();
      if (!TransitDataConstants.STATUS_CANCELED.equals(tripDetails.getStatus().getStatus())) {
        activity.setMonitoredVehicleJourney(new MonitoredVehicleJourney());
        SiriSupport.fillMonitoredVehicleJourney(activity.getMonitoredVehicleJourney(),
                tripDetails.getTrip(), tripDetails.getStatus(), null, OnwardCallsMode.VEHICLE_MONITORING,
                _presentationService, _transitDataService, maximumOnwardCalls,
                timePredictionRecords, tripDetails.getStatus().isPredicted(), currentTime, showRawLocation, showApc);
        output.add(activity);
      }

    }

    Collections.sort(output, new Comparator<VehicleActivityStructure>() {
      public int compare(VehicleActivityStructure arg0, VehicleActivityStructure arg1) {
        try {
          SiriExtensionWrapper wrapper0 = (SiriExtensionWrapper)arg0.getMonitoredVehicleJourney().getMonitoredCall().getExtensions().getAny();
          SiriExtensionWrapper wrapper1 = (SiriExtensionWrapper)arg1.getMonitoredVehicleJourney().getMonitoredCall().getExtensions().getAny();
          return wrapper0.getDistances().getDistanceFromCall().compareTo(wrapper1.getDistances().getDistanceFromCall());        
        } catch(Exception e) {
          return -1;
        }
      }
    });
    
    return output;
  }

  @Override
  public VehicleActivityStructure getVehicleActivityForVehicle(String vehicleId, int maximumOnwardCalls, long currentTime, String tripId) {    
	
	TripForVehicleQueryBean query = new TripForVehicleQueryBean();
    query.setTime(new Date(currentTime));
    query.setVehicleId(vehicleId);

    TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();
    inclusion.setIncludeTripStatus(true);
    inclusion.setIncludeTripBean(true);
    query.setInclusion(inclusion);
    
    TripDetailsBean tripDetailsForCurrentTrip = _transitDataService.getTripDetailsForVehicleAndTime(query);
    
    if(tripDetailsForCurrentTrip == null){
    	TripDetailsQueryBean detailsQuery = new TripDetailsQueryBean();
    	detailsQuery.setTime(currentTime);
    	detailsQuery.setTripId(tripId);
    	tripDetailsForCurrentTrip = _transitDataService.getSingleTripDetails(detailsQuery);
    }

    if(tripDetailsForCurrentTrip == null || !_presentationService.include(tripDetailsForCurrentTrip.getStatus()))
        return null;
    

    VehicleActivityStructure output = new VehicleActivityStructure();
    // Check for Realtime Data
    if(!tripDetailsForCurrentTrip.getStatus().isPredicted()){
    	output.setRecordedAtTime(new Date(getTime()));
    }
    else{
    	output.setRecordedAtTime(new Date(tripDetailsForCurrentTrip.getStatus().getLastUpdateTime()));
    }
    
    List<TimepointPredictionRecord> timePredictionRecords = null;
    timePredictionRecords = _transitDataService.getPredictionRecordsForTrip(AgencyAndId.convertFromString(vehicleId).getAgencyId(), tripDetailsForCurrentTrip.getStatus());

    boolean showApc = useApc();
    if (!TransitDataConstants.STATUS_CANCELED.equals(tripDetailsForCurrentTrip.getStatus())) {
      output.setMonitoredVehicleJourney(new MonitoredVehicleJourney());
      SiriSupport.fillMonitoredVehicleJourney(output.getMonitoredVehicleJourney(),
              tripDetailsForCurrentTrip.getTrip(), tripDetailsForCurrentTrip.getStatus(), null, OnwardCallsMode.VEHICLE_MONITORING,
              _presentationService, _transitDataService, maximumOnwardCalls,
              timePredictionRecords, tripDetailsForCurrentTrip.getStatus().isPredicted(), currentTime, false, showApc);
      return output;
    }
     return null;
  }

  @Override
  public List<MonitoredStopVisitStructure> getMonitoredStopVisitsForStop(String stopId, int maximumOnwardCalls, long currentTime) {
    List<MonitoredStopVisitStructure> output = new ArrayList<MonitoredStopVisitStructure>();

    for (ArrivalAndDepartureBean adBean : getArrivalsAndDeparturesForStop(stopId, currentTime)) {
      
      TripStatusBean statusBeanForCurrentTrip = adBean.getTripStatus();
      TripBean tripBeanForAd = adBean.getTrip();
      final RouteBean routeBean = tripBeanForAd.getRoute();
      
      if(statusBeanForCurrentTrip == null) {
        _log.debug("status drop");
    	  continue;
      }

      if(!_presentationService.include(statusBeanForCurrentTrip) || !_presentationService.include(adBean, statusBeanForCurrentTrip)) {
          _log.debug("presentation drop for vehicle=" + statusBeanForCurrentTrip.getVehicleId());
          continue;
      }
      
      if(!_transitDataService.stopHasRevenueServiceOnRoute((routeBean.getAgency()!=null?routeBean.getAgency().getId():null), 
  	    	  stopId, routeBean.getId(), adBean.getTrip().getDirectionId())) {
        _log.debug("non reveunue drop");
    	  continue;
      }
      
      // Filter out if the vehicle has realtime information and is ahead of current stop
      if (statusBeanForCurrentTrip.isPredicted() && !(adBean.hasPredictedArrivalTime() || adBean.hasPredictedDepartureTime())) {
        _log.debug("no realtime drop");
        continue;
      }
      if (statusBeanForCurrentTrip.getVehicleId() != null) {
        _log.debug("valid vehicle " + statusBeanForCurrentTrip.getVehicleId());
      }
      MonitoredStopVisitStructure stopVisit = new MonitoredStopVisitStructure();
     
      // Check for Realtime Data
      if(!statusBeanForCurrentTrip.isPredicted()){
    	  stopVisit.setRecordedAtTime(new Date(getTime()));
      }
      else{
    	  stopVisit.setRecordedAtTime(new Date(statusBeanForCurrentTrip.getLastUpdateTime()));
      }
  
      List<TimepointPredictionRecord> timePredictionRecords = null;
      timePredictionRecords = _transitDataService
              .getPredictionRecordsForTrip(AgencyAndId
                              .convertFromString(stopId).getAgencyId(),
                      statusBeanForCurrentTrip);

      boolean showApc = useApc();
      if (!TransitDataConstants.STATUS_CANCELED.equals(statusBeanForCurrentTrip.getStatus())) {
        stopVisit.setMonitoredVehicleJourney(new MonitoredVehicleJourneyStructure());
        SiriSupport.fillMonitoredVehicleJourney(stopVisit.getMonitoredVehicleJourney(),
                tripBeanForAd, statusBeanForCurrentTrip, adBean.getStop(), OnwardCallsMode.STOP_MONITORING,
                _presentationService, _transitDataService, maximumOnwardCalls,
                timePredictionRecords, statusBeanForCurrentTrip.isPredicted(), currentTime, false, showApc);
        output.add(stopVisit);
      }

    }
    
    Collections.sort(output, new Comparator<MonitoredStopVisitStructure>() {
        public int compare(MonitoredStopVisitStructure arg0, MonitoredStopVisitStructure arg1) {
          try {
            Date expectedArrival0 = arg0.getMonitoredVehicleJourney().getMonitoredCall().getExpectedArrivalTime();
    		    Date expectedArrival1 = arg1.getMonitoredVehicleJourney().getMonitoredCall().getExpectedArrivalTime();
            return expectedArrival0.compareTo(expectedArrival1);
          } catch(Exception e) {
            return -1;
          }
        }
    });
    
    return output;
  }

  /**
   * CURRENT IN-SERVICE VEHICLE STATUS FOR ROUTE
   */

  /**
   * Returns true if there are vehicles in service for given route+direction
   */
  
  @Override
  public boolean getVehiclesInServiceForRoute(String routeId, String directionId, long currentTime) {
	  ListBean<TripDetailsBean> trips = getAllTripsForRoute(routeId, currentTime);
	  for(TripDetailsBean tripDetails : trips.getList()) {
	    if(tripDetails == null)
	      continue;
		  // filter out interlined routes
		  if(routeId != null && !tripDetails.getTrip().getRoute().getId().equals(routeId))
			  continue;

		  // filtered out by direction
		  if (directionId != null && tripDetails.getTrip().getDirectionId() != null) {
		    if( !tripDetails.getTrip().getDirectionId().equals(directionId)) {
		      continue;
		    }
		  }
		  return true;
	  } 

	  return false;
  }

  /**
   * Returns true if there are vehicles in service for given route+direction that will stop
   * at the indicated stop in the future.
   */
  
  @Override
  public boolean getVehiclesInServiceForStopAndRoute(String stopId, String routeId, long currentTime) {
	  for (ArrivalAndDepartureBean adBean : getArrivalsAndDeparturesForStop(stopId, currentTime)) {
		  TripStatusBean statusBean = adBean.getTripStatus();
		  if(!_presentationService.include(statusBean) || !_presentationService.include(adBean, statusBean))
			  continue;

		  // filtered out by user
		  if(routeId != null && !adBean.getTrip().getRoute().getId().equals(routeId))
			  continue;

		  return true;
	  }

	  return false;
  }
  
  /**
   * SERVICE ALERTS METHODS
   */

  @Override
  public List<ServiceAlertBean> getServiceAlertsForAgency(String agencyId) {
    SituationQueryBean query = new SituationQueryBean();
    query.setTime(getTime());
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);

    affects.setAgencyId(agencyId);

    ListBean<ServiceAlertBean> serviceAlerts = _transitDataService.getServiceAlerts(query);
    return serviceAlerts.getList();
  }
  
  @Override
  public List<ServiceAlertBean> getServiceAlertsForRoute(String routeId) {
    return getServiceAlertsForRouteAndDirection(routeId, null); 
  }
  
  @Override
  public List<ServiceAlertBean> getServiceAlertsForRouteAndDirection(
      String routeId, String directionId) {
    SituationQueryBean query = new SituationQueryBean();
    query.setTime(getTime());
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);

    affects.setRouteId(routeId);
    if (directionId != null) {
      affects.setDirectionId(directionId);
    } else { 
      /*
       * TODO
       * The route index is not currently being populated correctly; query by route and direction,
       * and supply both directions if not present
       */
  
      SituationQueryBean.AffectsBean affects1 = new SituationQueryBean.AffectsBean();
      query.getAffects().add(affects1);
      affects1.setRouteId(routeId);
      affects1.setDirectionId("0");
      SituationQueryBean.AffectsBean affects2 = new SituationQueryBean.AffectsBean();
      query.getAffects().add(affects2);
      affects2.setRouteId(routeId);
      affects2.setDirectionId("1");
    }
    
    ListBean<ServiceAlertBean> serviceAlerts = _transitDataService.getServiceAlerts(query);
    return serviceAlerts.getList();
  }
  @Override
  public List<ServiceAlertBean> getServiceAlertsForRouteAndStop(
          String routeId, String stopId) {
    SituationQueryBean query = new SituationQueryBean();
    query.setTime(getTime());
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);

    affects.setRouteId(routeId);
    if (stopId != null) {
      affects.setStopId(stopId);
    }

    ListBean<ServiceAlertBean> serviceAlerts = _transitDataService.getServiceAlerts(query);
    return serviceAlerts.getList();
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsGlobal() {
    SituationQueryBean query = new SituationQueryBean();
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.setTime(getTime());
    
    affects.setAgencyId("__ALL_OPERATORS__");
    query.getAffects().add(affects);

    ListBean<ServiceAlertBean> serviceAlerts = _transitDataService.getServiceAlerts(query);
    return serviceAlerts.getList();
  }

  @Override
  public boolean showApc(){
    if(!useApc()){
      return false;
    }
    // here we could optionally allow access to specific API keys
//    String apc = _configurationService.getConfigurationValueAsString("display.validApcKeys", "");
//    List<String> keys = Arrays.asList(apc.split("\\s*;\\s*"));
//    for(String key : keys){
//      if(key.trim().equals("*")){
//        return true;
//      }
//    }
//    return false;
    return true;
  }


  /**
   * PRIVATE METHODS
   */
  
  private ListBean<TripDetailsBean> getAllTripsForRoute(String routeId, long currentTime) {
    TripsForRouteQueryBean tripRouteQueryBean = new TripsForRouteQueryBean();
    tripRouteQueryBean.setRouteId(routeId);
    tripRouteQueryBean.setTime(currentTime);
    
    TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
    inclusionBean.setIncludeTripBean(true);
    inclusionBean.setIncludeTripStatus(true);
    tripRouteQueryBean.setInclusion(inclusionBean);

    return _transitDataService.getTripsForRoute(tripRouteQueryBean);
  } 
  
  private List<ArrivalAndDepartureBean> getArrivalsAndDeparturesForStop(String stopId, long currentTime) {
    ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
    query.setTime(currentTime);
    query.setMinutesBefore(5);
    query.setMinutesAfter(65);
    
    StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures =
      _transitDataService.getStopWithArrivalsAndDepartures(stopId, query);

    return stopWithArrivalsAndDepartures.getArrivalsAndDepartures();
  }


  private boolean useApc(){
    // cache this value as its called frequently
    if (_useApc == null)
      _useApc = _configurationService.getConfigurationValueAsBoolean("tds.useApc", Boolean.TRUE);
    return _useApc;
  }

}

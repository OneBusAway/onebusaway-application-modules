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
package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Affects;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Id;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlertsCollection;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.TimeRange;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ServiceAlertsServiceImpl implements ServiceAlertsService {

  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsServiceImpl.class);

	/**
	 * While a service alert affects clause ({@link Affects} or
	 * {@link SituationQueryBean.AffectsBean}) might specify any combination of
	 * agency, route, trip, stop, etc. in practice, we only support a couple of
	 * specific combinations for applying alerts. We enumerate those specific
	 * combinations here so that they can be identified in queries.
	 */
	private enum AffectsType {
		AGENCY, ROUTE, ROUTE_DIRECTION, ROUTE_STOP, ROUTE_DIRECTION_STOP, TRIP, TRIP_STOP, STOP, UNSUPPORTED
	}

	private ServiceAlertsCache _cache;
	
	private ServiceAlertsPersistence _persister;
	
	private FederatedTransitDataBundle _bundle;

	private File _serviceAlertsPath;

	@Autowired
	public void setServiceAlertsCache(ServiceAlertsCache cache) {
	  _cache = cache;
	}
	
	public ServiceAlertsCache getServiceAlertsCache() {
	  return _cache;
	}
	
	@Autowired
	public void setServiceAlertsPersistence(ServiceAlertsPersistence persister) {
	  _persister = persister;
	}
	
	public ServiceAlertsPersistence getServiceAlertsPeristence() {
	  return _persister;
	}
	
	@Autowired
	public void setBundle(FederatedTransitDataBundle bundle) {
		_bundle = bundle;
	}

	public void setServiceAlertsPath(File path) {
		_serviceAlertsPath = path;
	}
	
	@PostConstruct
	public void start() {
		loadServiceAlerts();
	}

	@PreDestroy
	public void stop() {
		_log.info("Stopping ServiceAlertsService");
	}

	/****
	 * {@link ServiceAlertsService} Interface
	 ****/

	@Override
	public synchronized ServiceAlert createOrUpdateServiceAlert(
			ServiceAlert.Builder builder, String defaultAgencyId) {

	  if (_persister.needsSync()) this.loadServiceAlerts();
	  
		if (!builder.hasId()) {
			UUID uuid = UUID.randomUUID();
			Id id = ServiceAlertLibrary.id(defaultAgencyId, uuid.toString());
			builder.setId(id);
		}

		if (!builder.hasCreationTime())
			builder.setCreationTime(System.currentTimeMillis());
		builder.setModifiedTime(System.currentTimeMillis());

		ServiceAlert serviceAlert = builder.build();
		updateReferences(serviceAlert);
		// for backwards compatibility, we update the serialized bundle file
		saveServiceAlerts();
		saveDBServiceAlerts(serviceAlert);
		return serviceAlert;
	}

	@Override
	public synchronized void removeServiceAlert(AgencyAndId serviceAlertId) {
		removeServiceAlerts(Arrays.asList(serviceAlertId));
	}

	@Override
	public synchronized void removeServiceAlerts(List<AgencyAndId> serviceAlertIds) {
	  if (_persister.needsSync()) this.loadServiceAlerts();
		for (AgencyAndId serviceAlertId : serviceAlertIds) {
			ServiceAlert existingServiceAlert = _cache.removeServiceAlert(serviceAlertId);

			if (existingServiceAlert != null) {
				updateReferences(existingServiceAlert, null);
			}
			
			//Now remove from the DataBase.
			ServiceAlertRecord existingServiceAlertRecord = getServiceAlertRecordByAlertId(serviceAlertId.getId());
			if (existingServiceAlertRecord != null) {
			  _persister.delete(existingServiceAlertRecord);
				
			}
		}    
	}

	@Override
	public synchronized void removeAllServiceAlertsForFederatedAgencyId(
			String agencyId) {
		Set<AgencyAndId> ids = _cache.getServiceAlertIdsByServiceAlertAgencyId().get(agencyId);
		if (ids != null)
			removeServiceAlerts(new ArrayList<AgencyAndId>(ids));
	}

	@Override
	public ServiceAlert getServiceAlertForId(AgencyAndId serviceAlertId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		return _cache.getServiceAlerts().get(serviceAlertId);
	}

	@Override
	public List<ServiceAlert> getAllServiceAlerts() {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		return new ArrayList<ServiceAlert>(_cache.getServiceAlerts().values());
	}

	@Override
	public List<ServiceAlert> getServiceAlertsForFederatedAgencyId(String agencyId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		Set<AgencyAndId> serviceAlertIds = _cache.getServiceAlertIdsByServiceAlertAgencyId().get(agencyId);
		return getServiceAlertIdsAsObjects(serviceAlertIds);
	}

	@Override
	public List<ServiceAlert> getServiceAlertsForAgencyId(long time,
			String agencyId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), agencyId,
				serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlert> getServiceAlertsForStopId(long time,
			AgencyAndId stopId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), stopId.getAgencyId(),
				serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByStopId(), stopId, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlert> getServiceAlertsForStopCall(long time,
			BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
			AgencyAndId vehicleId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
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
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteId(), lineId, serviceAlertIds);
		RouteAndStopCallRef routeAndStopCallRef = new RouteAndStopCallRef(lineId,
				stopId);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndStop(),
				routeAndStopCallRef, serviceAlertIds);

		/**
		 * Remember that direction is optional
		 */
		if (directionId != null) {
			RouteAndDirectionRef lineAndDirectionRef = new RouteAndDirectionRef(
					lineId, directionId);
			RouteDirectionAndStopCallRef lineDirectionAndStopCallRef = new RouteDirectionAndStopCallRef(
					lineId, directionId, stopId);

			getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndDirectionId(),
					lineAndDirectionRef, serviceAlertIds);
			getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteDirectionAndStopCall(),
					lineDirectionAndStopCallRef, serviceAlertIds);
		}

		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripId(), trip.getId(),
				serviceAlertIds);
		TripAndStopCallRef tripAndStopCallRef = new TripAndStopCallRef(tripId,
				stopId);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripAndStopId(),
				tripAndStopCallRef, serviceAlertIds);

		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlert> getServiceAlertsForVehicleJourney(long time,
			BlockTripInstance blockTripInstance, AgencyAndId vehicleId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		BlockTripEntry blockTrip = blockTripInstance.getBlockTrip();
		TripEntry trip = blockTrip.getTrip();
		AgencyAndId lineId = trip.getRouteCollection().getId();
		RouteAndDirectionRef lineAndDirectionRef = new RouteAndDirectionRef(lineId,
				trip.getDirectionId());

		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), lineId.getAgencyId(),
				serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteId(), lineId, serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndDirectionId(),
				lineAndDirectionRef, serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripId(), trip.getId(),
				serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlert> getServiceAlerts(SituationQueryBean query) {
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		for (SituationQueryBean.AffectsBean affects : query.getAffects()) {

			AgencyAndId routeId = AgencyAndId.convertFromString(affects.getRouteId());
			AgencyAndId tripId = AgencyAndId.convertFromString(affects.getTripId());
			AgencyAndId stopId = AgencyAndId.convertFromString(affects.getStopId());

			AffectsType type = getAffectsType(affects.getAgencyId(),
					affects.getRouteId(), affects.getDirectionId(), affects.getTripId(),
					affects.getStopId());
			switch (type) {
			case AGENCY: {
				/**
				 * Note we are treating the query's agency ID as that of what the
				 * service alert affects, not the alert's federated agency ID.
				 */
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(),
						affects.getAgencyId(), serviceAlertIds);
				break;
			}
			case ROUTE: {

				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteId(), routeId,
						serviceAlertIds);
				break;
			}
			case TRIP: {
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripId(), tripId,
						serviceAlertIds);
				break;
			}
			case STOP: {
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByStopId(), stopId,
						serviceAlertIds);
				break;
			}
			case ROUTE_DIRECTION: {
				RouteAndDirectionRef routeAndDirectionRef = new RouteAndDirectionRef(
						routeId, affects.getDirectionId());
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndDirectionId(),
						routeAndDirectionRef, serviceAlertIds);
				break;
			}
			case ROUTE_DIRECTION_STOP: {
				RouteDirectionAndStopCallRef ref = new RouteDirectionAndStopCallRef(
						routeId, affects.getDirectionId(), stopId);
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteDirectionAndStopCall(),
						ref, serviceAlertIds);
				break;
			}
			case ROUTE_STOP: {
				RouteAndStopCallRef routeAndStopRef = new RouteAndStopCallRef(
						routeId, stopId);
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndStop(),
						routeAndStopRef, serviceAlertIds);
				break;
			}
			case TRIP_STOP: {
				TripAndStopCallRef ref = new TripAndStopCallRef(tripId, stopId);
				getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripAndStopId(), ref,
						serviceAlertIds);
				break;
			}
			default: {
				throw new RuntimeException("Unhandled type " + type);
			}
			}
		}

		List<ServiceAlert> alerts = getServiceAlertIdsAsObjects(serviceAlertIds);

		// SituationQueryBean no longer supports filtering by time, but it might return, so leaving this code here
		// for future reference.
		//    filterByTime(query, alerts);

		return alerts;
	}

	/****
	 * Private Methods
	 ****/

	// SituationQUeryBean no longer supports filtering by time, but it might return, so leaving this code here
	// for future reference.
	//  private void filterByTime(SituationQueryBean query, List<ServiceAlert> alerts) {
	//    long queryTime = query.getTime();
	//    if (queryTime != 0) {
	//      Predicate predicate = new Predicate() {
	//        private long queryTime;
	//        @Override
	//        public boolean evaluate(Object arg0) {
	//          ServiceAlert alert = (ServiceAlert)arg0;
	//          List<TimeRange> list = alert.getPublicationWindowList();
	//          if (list.isEmpty()) {
	//            return true;
	//          }
	//          // If we're inside *any* publication window, return true
	//          for (TimeRange t: list) {
	//            if ((t.getStart() <= queryTime) && (queryTime <= t.getEnd())) {
	//              return true;
	//            }
	//          }
	//          return false;
	//        }
	//
	//        public Predicate init(long queryTime) {
	//          this.queryTime = queryTime;
	//          return this;
	//        }
	//      }.init(queryTime);
	//      CollectionUtils.filter(alerts, predicate );
	//    }
	//  }
	//
	private void updateReferences(ServiceAlert serviceAlert) {
		AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getId());
		ServiceAlert existingServiceAlert = _cache.putServiceAlert(id, serviceAlert);
		updateReferences(existingServiceAlert, serviceAlert);
	}

	private void updateReferences(ServiceAlert existingServiceAlert,
			ServiceAlert serviceAlert) {

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByServiceAlertAgencyId(),
				AffectsServiceAlertAgencyKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByAgencyId(), AffectsAgencyKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByStopId(), AffectsStopKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByRouteId(), AffectsRouteKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByRouteAndDirectionId(),
				AffectsRouteAndDirectionKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByRouteAndStop(), AffectsRouteAndStopKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByRouteDirectionAndStopCall(),
				AffectsRouteDirectionAndStopCallKeyFactory.INSTANCE);

		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByTripId(), AffectsTripKeyFactory.INSTANCE);
		updateReferences(existingServiceAlert, serviceAlert,
				_cache.getServiceAlertIdsByTripAndStopId(), AffectsTripAndStopKeyFactory.INSTANCE);

	}

	private <T> void updateReferences(ServiceAlert existingServiceAlert,
			ServiceAlert serviceAlert, Map<T, Set<AgencyAndId>> map,
			AffectsKeyFactory<T> affectsKeyFactory) {

		Set<T> existingEffects = Collections.emptySet();
		if (existingServiceAlert != null) {
			existingEffects = affectsKeyFactory.getKeysForAffects(existingServiceAlert);
		}

		Set<T> newEffects = Collections.emptySet();
		if (serviceAlert != null) {
			newEffects = affectsKeyFactory.getKeysForAffects(serviceAlert);
		}

		for (T existingEffect : existingEffects) {
			if (newEffects.contains(existingEffect))
				continue;
			AgencyAndId id = ServiceAlertLibrary.agencyAndId(existingServiceAlert.getId());
			Set<AgencyAndId> ids = map.get(existingEffect);
			ids.remove(id);
			if (ids.isEmpty())
				map.remove(existingEffect);
		}

		for (T newEffect : newEffects) {
			if (existingEffects.contains(newEffect))
				continue;
			AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getId());
			Set<AgencyAndId> ids = map.get(newEffect);
			if (ids == null) {
				ids = new HashSet<AgencyAndId>();
				map.put(newEffect, ids);
			}
			ids.add(id);
		}
	}

	private <T> void getServiceAlertIdsForKey(
			Map<T, Set<AgencyAndId>> serviceAlertIdsByKey, T key,
			Collection<AgencyAndId> matches) {
		Set<AgencyAndId> ids = serviceAlertIdsByKey.get(key);
		if (ids != null)
			matches.addAll(ids);
	}

	private List<ServiceAlert> getServiceAlertIdsAsObjects(
			Collection<AgencyAndId> serviceAlertIds) {
		return getServiceAlertIdsAsObjects(serviceAlertIds, -1);
	}

	private List<ServiceAlert> getServiceAlertIdsAsObjects(
			Collection<AgencyAndId> serviceAlertIds, long time) {
		if (serviceAlertIds == null || serviceAlertIds.isEmpty())
			return Collections.emptyList();
		List<ServiceAlert> serviceAlerts = new ArrayList<ServiceAlert>(
				serviceAlertIds.size());
		for (AgencyAndId serviceAlertId : serviceAlertIds) {
			ServiceAlert serviceAlert = _cache.getServiceAlerts().get(serviceAlertId);
			if (serviceAlert != null && filterByTime(serviceAlert, time))
				serviceAlerts.add(serviceAlert);
		}
		return serviceAlerts;
	}

	private boolean filterByTime(ServiceAlert serviceAlert, long time) {
		if (time == -1 || serviceAlert.getPublicationWindowList().size() == 0)
			return true;
		for (TimeRange publicationWindow : serviceAlert.getPublicationWindowList()) {
			if ((!publicationWindow.hasStart() || publicationWindow.getStart() <= time)
					&& (!publicationWindow.hasEnd() || publicationWindow.getEnd() >= time)) {
				return true;
			}
		}
		return false;
	}

	private AffectsType getAffectsType(String agencyId, String routeId,
			String directionId, String tripId, String stopId) {
		int count = getNonNullCount(agencyId, routeId, directionId, tripId, stopId);
		switch (count) {
		case 0: {
			_log.warn("no arguments specified in affects clause");
			break;
		}
		case 1: {
			if (agencyId != null) {
				return AffectsType.AGENCY;
			}
			if (routeId != null) {
				return AffectsType.ROUTE;
			}
			if (tripId != null) {
				return AffectsType.TRIP;
			}
			if (stopId != null) {
				return AffectsType.STOP;
			}
			break;
		}
		case 2: {
			if (routeId != null && directionId != null) {
				return AffectsType.ROUTE_DIRECTION;
			}
			if (routeId != null && stopId != null) {
				return AffectsType.ROUTE_STOP;
			}
			if (tripId != null && stopId != null) {
				return AffectsType.TRIP_STOP;
			}
			break;
		}
		case 3: {
			if (routeId != null && directionId != null && stopId != null) {
				return AffectsType.ROUTE_DIRECTION_STOP;
			}
			break;
		}
		}
		_log.warn("unsupported affects clause: agencyId=" + agencyId + " routeId="
				+ routeId + " directionId=" + directionId + " tripId=" + tripId
				+ " stopId=" + stopId);
		return AffectsType.UNSUPPORTED;
	}

	private int getNonNullCount(String... ids) {
		int count = 0;
		for (String id : ids) {
			if (id != null) {
				count++;
			}
		}
		return count;
	}

	/****
	 * Serialization
	 ****/

	synchronized void loadServiceAlerts() {
	  
	  _cache.clear(); //we need to clear the cache in case records were deleted
		List<ServiceAlertRecord> alerts = _persister.getAlerts();
		_log.debug("Loaded " + alerts.size() + " service alerts from DB");
		try {			
			for (ServiceAlertRecord serviceAlert : alerts)
				updateReferences(serviceAlert.getServiceAlert());

		} catch (Exception ex) {
			_log.error("error loading service alerts from DB " + ex.toString());
		}		
	}

	private synchronized void saveServiceAlerts() {
		File path = getServiceAlertsPath();

		if (path == null)
			return;

		ServiceAlertsCollection.Builder builder = ServiceAlertsCollection.newBuilder();
		builder.addAllServiceAlerts(_cache.getServiceAlerts().values());
		ServiceAlertsCollection collection = builder.build();

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(path));
			collection.writeTo(out);
			out.close();
		} catch (Exception ex) {
			_log.error("error saving service alerts to path " + path, ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					_log.error("error closing service output to path " + path, ex);
				}
			}
		}
	}

	// this is admittedly slow performing, but it is only called on an update
	// of a single service alert
	private synchronized void saveDBServiceAlerts(ServiceAlert alert) {
		
		AgencyAndId alertId = ServiceAlertLibrary.agencyAndId(alert.getId());
		ServiceAlertRecord record = getServiceAlertRecordByAlertId(alert.getId().getId());
		if(record == null) {
			record = new ServiceAlertRecord(); 
		}
		
		record.setServiceAlertId(alertId.getId());
		record.setAgencyId(ServiceAlertLibrary.agencyAndId(alert.getId()));			
		record.setServiceAlert(alert);
    _log.info("Saving Service Alert to DataBase:" + alertId.getId());     

		_persister.saveOrUpdate(record);
	}

	/**
	 * Following method is looking into the database and pulling out the record related with the service ID passed.
	 * @param id
	 * @return
	 */
	private ServiceAlertRecord getServiceAlertRecordByAlertId(final String uuid) {
	  return _persister.getServiceAlertRecordByAlertId(uuid);
	}	

	private File getServiceAlertsPath() {
		if (_serviceAlertsPath != null)
			return _serviceAlertsPath;
		return _bundle.getServiceAlertsPath();
	}

}

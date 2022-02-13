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
package org.onebusaway.alerts.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.alerts.service.ServiceAlerts.Affects;
import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.*;

@Component
public class ServiceAlertsServiceImpl implements ServiceAlertsService {

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

	@PostConstruct
	public void start() {
	  try {
	    loadServiceAlerts();
	  } catch (Throwable t) {
	    _log.error("issue loading service alerts: ", t);
	  }
	}

	@PreDestroy
	public void stop() {
		_log.info("Stopping ServiceAlertsService");
	}

	/****
	 * {@link ServiceAlertsService} Interface
	 ****/

	@Override
	public synchronized ServiceAlertRecord createOrUpdateServiceAlert(
      ServiceAlertRecord serviceAlertRecord) {

	  if (_persister.needsSync()) this.loadServiceAlerts();
	  
		if (serviceAlertRecord.getServiceAlertId() == null) {
			UUID uuid = UUID.randomUUID();
			serviceAlertRecord.setServiceAlertId(uuid.toString());
		}

		long lastModified = SystemTime.currentTimeMillis();
		if (serviceAlertRecord.getCreationTime() < 1l)
        serviceAlertRecord.setCreationTime(lastModified);

		updateReferences(serviceAlertRecord);
		saveDBServiceAlerts(serviceAlertRecord, lastModified);
		return serviceAlertRecord;
	}

	@Override
	public synchronized List<ServiceAlertRecord> createOrUpdateServiceAlerts(String agencyId, List<ServiceAlertRecord> records) {
		if (_persister.needsSync()) this.loadServiceAlerts();
		long lastModified = SystemTime.currentTimeMillis();

		for (ServiceAlertRecord serviceAlertRecord : records) {
			if (serviceAlertRecord.getServiceAlertId() == null) {
				UUID uuid = UUID.randomUUID();
				serviceAlertRecord.setServiceAlertId(uuid.toString());
			}


			if (serviceAlertRecord.getCreationTime() < 1l)
				serviceAlertRecord.setCreationTime(lastModified);

			updateReferences(serviceAlertRecord);
		}
		saveDBServiceAlerts(records, lastModified);
		return records;

	}


	@Override
	public synchronized ServiceAlertRecord copyServiceAlert(ServiceAlertRecord serviceAlertRecord) {
		
		if (_persister.needsSync()) this.loadServiceAlerts();
		UUID uuid = UUID.randomUUID();
		serviceAlertRecord.setServiceAlertId(uuid.toString());

		long lastModified = SystemTime.currentTimeMillis();
        serviceAlertRecord.setCreationTime(lastModified);
        serviceAlertRecord.setCopy(Boolean.TRUE);
		
		updateReferences(serviceAlertRecord);
		saveDBServiceAlerts(serviceAlertRecord, lastModified);
		return serviceAlertRecord;
	}	

	@Override
	public synchronized void removeServiceAlert(AgencyAndId serviceAlertId) {
		removeServiceAlerts(Arrays.asList(serviceAlertId));
	}

	@Override
	public synchronized void removeServiceAlerts(List<AgencyAndId> serviceAlertIds) {
	  if (_persister.needsSync()) this.loadServiceAlerts();
		for (AgencyAndId serviceAlertId : serviceAlertIds) {
      ServiceAlertRecord existingServiceAlert = _cache.removeServiceAlert(serviceAlertId);

			if (existingServiceAlert != null) {
				updateReferences(existingServiceAlert, null);
			}
			
			//Now remove from the DataBase.
			ServiceAlertRecord existingServiceAlertRecord = getServiceAlertRecordByAlertId(serviceAlertId.getAgencyId(), serviceAlertId.getId());
			_log.debug("deleting service alert " + serviceAlertId.getId());
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
	public ServiceAlertRecord getServiceAlertForId(AgencyAndId serviceAlertId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		return _cache.getServiceAlerts().get(serviceAlertId);
	}

	@Override
	public List<ServiceAlertRecord> getAllServiceAlerts() {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		return new ArrayList<ServiceAlertRecord>(_cache.getServiceAlerts().values());
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForFederatedAgencyId(String agencyId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		Set<AgencyAndId> serviceAlertIds = _cache.getServiceAlertIdsByServiceAlertAgencyId().get(agencyId);
		return getServiceAlertIdsAsObjects(serviceAlertIds);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForAgencyId(long time,
			String agencyId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), agencyId,
				serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForStopId(long time,
			AgencyAndId stopId) {
	  if (_persister.cachedNeedsSync()) this.loadServiceAlerts();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), stopId.getAgencyId(),
				serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByStopId(), stopId, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForRouteId(long time, AgencyAndId routeId) {
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteId(), routeId, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForRouteAndStopId(long time, AgencyAndId routeId, AgencyAndId stopId) {
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		RouteAndStopCallRef routeAndStopCallRef = new RouteAndStopCallRef(routeId,
				stopId);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndStop(),
				routeAndStopCallRef, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	public List<ServiceAlertRecord> getServiceAlertsForRouteAndDirection(long time, AgencyAndId routeId, AgencyAndId tripId, String directionId) {
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		RouteAndDirectionRef lineAndDirectionRef = new RouteAndDirectionRef(routeId,
				directionId);


		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), routeId.getAgencyId(),
				serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteId(), routeId, serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndDirectionId(),
				lineAndDirectionRef, serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripId(), tripId,
				serviceAlertIds);

		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForTripAndStopId(long time, AgencyAndId tripId, AgencyAndId stopId) {
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripId(), tripId,
				serviceAlertIds);
		TripAndStopCallRef tripAndStopCallRef = new TripAndStopCallRef(tripId,
				stopId);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByTripAndStopId(),
				tripAndStopCallRef, serviceAlertIds);

		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlerts(SituationQueryBean query) {
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

		List<ServiceAlertRecord> alerts = getServiceAlertIdsAsObjects(serviceAlertIds, query.getTime());

		return alerts;
	}

	/****
	 * Private Methods
	 ****/

	private void updateReferences(ServiceAlertRecord serviceAlert) {
		AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getAgencyId(), serviceAlert.getServiceAlertId());
    	ServiceAlertRecord existingServiceAlert = _cache.putServiceAlert(id, serviceAlert);
		updateReferences(existingServiceAlert, serviceAlert);
	}

	private void updateReferences(ServiceAlertRecord existingServiceAlert,
      ServiceAlertRecord serviceAlert) {

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

	private <T> void updateReferences(ServiceAlertRecord existingServiceAlert,
			ServiceAlertRecord serviceAlert, Map<T, Set<AgencyAndId>> map,
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
			AgencyAndId id = ServiceAlertLibrary.agencyAndId(existingServiceAlert.getAgencyId(), existingServiceAlert.getServiceAlertId());
			Set<AgencyAndId> ids = map.get(existingEffect);
			ids.remove(id);
			if (ids.isEmpty())
				map.remove(existingEffect);
			}

		for (T newEffect : newEffects) {
			if (existingEffects.contains(newEffect))
				continue;
      		AgencyAndId id = ServiceAlertLibrary.agencyAndId(serviceAlert.getAgencyId(), serviceAlert.getServiceAlertId());
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

	private List<ServiceAlertRecord> getServiceAlertIdsAsObjects(
			Collection<AgencyAndId> serviceAlertIds) {
		return getServiceAlertIdsAsObjects(serviceAlertIds, -1);
	}

	private List<ServiceAlertRecord> getServiceAlertIdsAsObjects(
			Collection<AgencyAndId> serviceAlertIds, long time) {
		if (serviceAlertIds == null || serviceAlertIds.isEmpty())
			return Collections.emptyList();
		List<ServiceAlertRecord> serviceAlerts = new ArrayList<ServiceAlertRecord>(
				serviceAlertIds.size());
		for (AgencyAndId serviceAlertId : serviceAlertIds) {
			ServiceAlertRecord serviceAlert = _cache.getServiceAlerts().get(serviceAlertId);
			if (serviceAlert != null && filterByTime(serviceAlert, time))
				serviceAlerts.add(serviceAlert);
		}
		return serviceAlerts;
	}

	private boolean filterByTime(ServiceAlertRecord serviceAlert, long time) {
		if (time == -1 || serviceAlert.getPublicationWindows().size() == 0)
			return true;
		for (ServiceAlertTimeRange publicationWindow : serviceAlert.getPublicationWindows()) {
			if ((publicationWindow.getFromValue() == null || publicationWindow.getFromValue() <= time)
					&& (publicationWindow.getToValue() == null || publicationWindow.getToValue() >= time)) {
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

	@Override
	public synchronized void cleanup() {
		_persister.deleteOrphans();
	}

	@Override
	public synchronized void loadServiceAlerts() {
	  _cache.clear(); //we need to clear the cache in case records were deleted
		List<ServiceAlertRecord> alerts = _persister.getAlerts();
		_log.debug("Loaded " + alerts.size() + " service alerts from DB");
		try {			
			for (ServiceAlertRecord serviceAlert : alerts) {
				updateReferences(serviceAlert);
			}
			_log.info("updateReferences complete with "
					+ _cache.getServiceAlertIdsByAgencyId().size() + " agency alerts, "
					+ _cache.getServiceAlertIdsByRouteAndStop().size() + " route and stop alerts, "
					+ _cache.getServiceAlertIdsByRouteId().size() + " route alerts, and "
					+ _cache.getServiceAlerts().size() + " distinct alerts");


		} catch (Exception ex) {
			_log.error("error loading service alerts from DB ", ex);
		}
	}

	// this is admittedly slow performing, but it is only called on an update
	// of a single service alert
	private synchronized void saveDBServiceAlerts(ServiceAlertRecord alert, Long lastModified) {
          if (lastModified == null) lastModified = SystemTime.currentTimeMillis();
          alert.setModifiedTime(lastModified); // we need to assume its changed, as we don't track the affects clause
          ServiceAlertRecord persistedServiceAlertRecord = _persister.getServiceAlertRecordByAlertId(alert.getAgencyId(), alert.getServiceAlertId());
          if(persistedServiceAlertRecord != null)
              alert.setId(persistedServiceAlertRecord.getId());
          _log.debug("Saving Service Alert to DataBase:" + alert.getServiceAlertId());
          _persister.saveOrUpdate(alert);
	}

	private synchronized void saveDBServiceAlerts(List<ServiceAlertRecord> alerts, Long lastModified) {
		if (lastModified == null) lastModified = SystemTime.currentTimeMillis();
		for (ServiceAlertRecord alert : alerts) {
			alert.setModifiedTime(lastModified); // we need to assume its changed, as we don't track the affects clause
			ServiceAlertRecord persistedServiceAlertRecord = _persister.getServiceAlertRecordByAlertId(alert.getAgencyId(), alert.getServiceAlertId());
			if (persistedServiceAlertRecord != null)
				alert.setId(persistedServiceAlertRecord.getId());
			_log.debug("Saving Service Alert to DataBase:" + alert.getServiceAlertId());
		}
		_persister.saveOrUpdate(alerts);
	}

	/**
	 * Following method is looking into the database and pulling out the record related with the service ID passed.
	 * @return
	 */
	private ServiceAlertRecord getServiceAlertRecordByAlertId(String agencyId, String serviceAlertId) {
	  return _persister.getServiceAlertRecordByAlertId(agencyId, serviceAlertId);
	}

}

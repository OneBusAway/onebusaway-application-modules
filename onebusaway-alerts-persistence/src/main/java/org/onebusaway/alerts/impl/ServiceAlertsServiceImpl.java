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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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

	/**
	 * Serializes writers, which do database work. Readers never take it, so a
	 * realtime alerts refresh can't stall arrivals or trip details.
	 */
	private final ReentrantLock _writeLock = new ReentrantLock();

	/**
	 * Guards the in-memory indexes. Held only for in-memory work, never across
	 * a database call.
	 */
	private final Object _indexLock = new Object();

	/**
	 * Checks the database for changes made elsewhere (another instance, the
	 * admin console) and reloads, off the request threads.
	 */
	private Executor _syncExecutor = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "service-alerts-sync");
		thread.setDaemon(true);
		return thread;
	});

	private final AtomicBoolean _syncQueued = new AtomicBoolean(false);

	/**
	 * Reads before this time don't queue a sync check. The persister throttles
	 * the database check itself; this keeps busy readers from waking the sync
	 * thread on every request just to learn nothing is due.
	 */
	private static final long SYNC_CHECK_INTERVAL_MS = 250;

	private volatile long _nextSyncCheck = 0;

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
	
	/**
	 * Where reader-triggered sync checks run. Tests that write through a
	 * transaction other threads can't see pass a same-thread executor.
	 */
	public void setSyncExecutor(Executor executor) {
		_syncExecutor = executor;
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
		if (_syncExecutor instanceof ExecutorService)
			((ExecutorService) _syncExecutor).shutdownNow();
	}

	/****
	 * {@link ServiceAlertsService} Interface
	 ****/

	@Override
	public ServiceAlertRecord createOrUpdateServiceAlert(
      ServiceAlertRecord serviceAlertRecord) {
		_writeLock.lock();
		try {
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
		} finally {
			_writeLock.unlock();
		}
	}

	@Override
	public List<ServiceAlertRecord> createOrUpdateServiceAlerts(String agencyId, List<ServiceAlertRecord> records) {
		if (records.isEmpty()) return records;
		_writeLock.lock();
		try {
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
		} finally {
			_writeLock.unlock();
		}
	}


	@Override
	public ServiceAlertRecord copyServiceAlert(ServiceAlertRecord serviceAlertRecord) {
		_writeLock.lock();
		try {
			if (_persister.needsSync()) this.loadServiceAlerts();
			UUID uuid = UUID.randomUUID();
			serviceAlertRecord.setServiceAlertId(uuid.toString());

			long lastModified = SystemTime.currentTimeMillis();
			serviceAlertRecord.setCreationTime(lastModified);
			serviceAlertRecord.setCopy(Boolean.TRUE);

			updateReferences(serviceAlertRecord);
			saveDBServiceAlerts(serviceAlertRecord, lastModified);
			return serviceAlertRecord;
		} finally {
			_writeLock.unlock();
		}
	}	

	@Override
	public void removeServiceAlert(AgencyAndId serviceAlertId) {
		removeServiceAlerts(Arrays.asList(serviceAlertId));
	}

	@Override
	public void removeServiceAlerts(List<AgencyAndId> serviceAlertIds) {
		if (serviceAlertIds.isEmpty()) return;
		_writeLock.lock();
		try {
			if (_persister.needsSync()) this.loadServiceAlerts();
			for (AgencyAndId serviceAlertId : serviceAlertIds) {
				synchronized (_indexLock) {
					ServiceAlertRecord existingServiceAlert = _cache.removeServiceAlert(serviceAlertId);
					if (existingServiceAlert != null) {
						updateReferences(existingServiceAlert, null);
					}
				}

				//Now remove from the DataBase.
				ServiceAlertRecord existingServiceAlertRecord = getServiceAlertRecordByAlertId(serviceAlertId.getAgencyId(), serviceAlertId.getId());
				_log.debug("deleting service alert " + serviceAlertId.getId());
				if (existingServiceAlertRecord != null) {
					_persister.delete(existingServiceAlertRecord);
				}
			}
		} finally {
			_writeLock.unlock();
		}
	}

	@Override
	public void removeAllServiceAlertsForFederatedAgencyId(
			String agencyId) {
		List<AgencyAndId> ids = new ArrayList<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByServiceAlertAgencyId(), agencyId, ids);
		removeServiceAlerts(ids);
	}

	@Override
	public ServiceAlertRecord getServiceAlertForId(AgencyAndId serviceAlertId) {
		reloadIfStale();
		synchronized (_indexLock) {
			return _cache.getServiceAlerts().get(serviceAlertId);
		}
	}

	@Override
	public List<ServiceAlertRecord> getAllServiceAlerts() {
		reloadIfStale();
		synchronized (_indexLock) {
			return new ArrayList<ServiceAlertRecord>(_cache.getServiceAlerts().values());
		}
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForFederatedAgencyId(String agencyId) {
		reloadIfStale();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByServiceAlertAgencyId(), agencyId,
				serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForAgencyId(long time,
			String agencyId) {
		reloadIfStale();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), agencyId,
				serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForStopId(long time,
			AgencyAndId stopId) {
		reloadIfStale();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByAgencyId(), stopId.getAgencyId(),
				serviceAlertIds);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByStopId(), stopId, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForRouteId(long time, AgencyAndId routeId) {
		reloadIfStale();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteId(), routeId, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	@Override
	public List<ServiceAlertRecord> getServiceAlertsForRouteAndStopId(long time, AgencyAndId routeId, AgencyAndId stopId) {
		reloadIfStale();
		Set<AgencyAndId> serviceAlertIds = new HashSet<AgencyAndId>();
		RouteAndStopCallRef routeAndStopCallRef = new RouteAndStopCallRef(routeId,
				stopId);
		getServiceAlertIdsForKey(_cache.getServiceAlertIdsByRouteAndStop(),
				routeAndStopCallRef, serviceAlertIds);
		return getServiceAlertIdsAsObjects(serviceAlertIds, time);
	}

	public List<ServiceAlertRecord> getServiceAlertsForRouteAndDirection(long time, AgencyAndId routeId, AgencyAndId tripId, String directionId) {
		reloadIfStale();
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
		reloadIfStale();
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
		reloadIfStale();
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
		synchronized (_indexLock) {
			ServiceAlertRecord existingServiceAlert = _cache.putServiceAlert(id, serviceAlert);
			updateReferences(existingServiceAlert, serviceAlert);
		}
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
		if (serviceAlertIdsByKey == null)
			return;
		synchronized (_indexLock) {
			Set<AgencyAndId> ids = serviceAlertIdsByKey.get(key);
			if (ids != null)
				matches.addAll(ids);
		}
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
		synchronized (_indexLock) {
			for (AgencyAndId serviceAlertId : serviceAlertIds) {
				ServiceAlertRecord serviceAlert = _cache.getServiceAlerts().get(serviceAlertId);
				if (serviceAlert != null && filterByTime(serviceAlert, time))
					serviceAlerts.add(serviceAlert);
			}
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
	public void cleanup() {
		_writeLock.lock();
		try {
			_persister.deleteOrphans();
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Readers serve the current cache and leave the staleness check and any
	 * reload to the sync thread, so no request waits on the database. At most
	 * one check is queued at a time.
	 */
	private void reloadIfStale() {
		long now = SystemTime.currentTimeMillis();
		if (now < _nextSyncCheck) return;
		if (!_syncQueued.compareAndSet(false, true)) return;
		_nextSyncCheck = now + SYNC_CHECK_INTERVAL_MS;
		try {
			_syncExecutor.execute(() -> {
				try {
					if (_persister.cachedNeedsSync()) loadServiceAlerts();
				} catch (Throwable t) {
					_log.error("issue syncing service alerts: ", t);
				} finally {
					_syncQueued.set(false);
				}
			});
		} catch (RuntimeException e) {
			// executor shut down
			_syncQueued.set(false);
		}
	}

	@Override
	public void loadServiceAlerts() {
		_writeLock.lock();
		try {
			// read before taking the index lock so readers keep the old cache meanwhile
			List<ServiceAlertRecord> alerts = _persister.getAlerts();
			_log.debug("Loaded " + alerts.size() + " service alerts from DB");
			try {
				synchronized (_indexLock) {
					_cache.clear(); //we need to clear the cache in case records were deleted
					for (ServiceAlertRecord serviceAlert : alerts) {
						updateReferences(serviceAlert);
					}
				}
				_log.info("updateReferences complete with "
						+ _cache.getServiceAlertIdsByAgencyId().size() + " agency alerts, "
						+ _cache.getServiceAlertIdsByRouteAndStop().size() + " route and stop alerts, "
						+ _cache.getServiceAlertIdsByRouteId().size() + " route alerts, and "
						+ _cache.getServiceAlerts().size() + " distinct alerts");
			} catch (Exception ex) {
				_log.error("error loading service alerts from DB ", ex);
			}
		} finally {
			_writeLock.unlock();
		}
	}

	// this is admittedly slow performing, but it is only called on an update
	// of a single service alert
	private void saveDBServiceAlerts(ServiceAlertRecord alert, Long lastModified) {
          if (lastModified == null) lastModified = SystemTime.currentTimeMillis();
          alert.setModifiedTime(lastModified); // we need to assume its changed, as we don't track the affects clause
          ServiceAlertRecord persistedServiceAlertRecord = _persister.getServiceAlertRecordByAlertId(alert.getAgencyId(), alert.getServiceAlertId());
          if(persistedServiceAlertRecord != null)
              alert.setId(persistedServiceAlertRecord.getId());
          _log.debug("Saving Service Alert to DataBase:" + alert.getServiceAlertId());
          _persister.saveOrUpdate(alert);
	}

	private void saveDBServiceAlerts(List<ServiceAlertRecord> alerts, Long lastModified) {
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

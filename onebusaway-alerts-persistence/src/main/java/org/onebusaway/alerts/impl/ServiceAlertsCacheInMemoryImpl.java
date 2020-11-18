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
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

@Component
public class ServiceAlertsCacheInMemoryImpl implements ServiceAlertsCache {

  private final Semaphore _available = new Semaphore(1, true);
  
  private ConcurrentMap<AgencyAndId, ServiceAlertRecord> _serviceAlerts = new ConcurrentHashMap<AgencyAndId, ServiceAlertRecord>();

  private ConcurrentMap<String, Set<AgencyAndId>> _serviceAlertIdsByServiceAlertAgencyId = new ConcurrentHashMap<String, Set<AgencyAndId>>();

  private ConcurrentMap<String, Set<AgencyAndId>> _serviceAlertIdsByAgencyId = new ConcurrentHashMap<String, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _serviceAlertIdsByStopId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _serviceAlertIdsByRouteId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<RouteAndDirectionRef, Set<AgencyAndId>> _serviceAlertIdsByRouteAndDirectionId = new ConcurrentHashMap<RouteAndDirectionRef, Set<AgencyAndId>>();

  private ConcurrentMap<RouteAndStopCallRef, Set<AgencyAndId>> _serviceAlertIdsByRouteAndStop = new ConcurrentHashMap<RouteAndStopCallRef, Set<AgencyAndId>>();

  private ConcurrentMap<RouteDirectionAndStopCallRef, Set<AgencyAndId>> _serviceAlertIdsByRouteDirectionAndStopCall = new ConcurrentHashMap<RouteDirectionAndStopCallRef, Set<AgencyAndId>>();

  private ConcurrentMap<AgencyAndId, Set<AgencyAndId>> _serviceAlertIdsByTripId = new ConcurrentHashMap<AgencyAndId, Set<AgencyAndId>>();

  private ConcurrentMap<TripAndStopCallRef, Set<AgencyAndId>> _serviceAlertIdsByTripAndStopId = new ConcurrentHashMap<TripAndStopCallRef, Set<AgencyAndId>>();

  @Override
  public void clear() {
    try {
      _available.acquire();
      _serviceAlerts.clear();
      _serviceAlertIdsByServiceAlertAgencyId.clear();
      _serviceAlertIdsByAgencyId.clear();
      _serviceAlertIdsByStopId.clear();
      _serviceAlertIdsByRouteId.clear();
      _serviceAlertIdsByRouteAndDirectionId.clear();
      _serviceAlertIdsByRouteAndStop.clear();
      _serviceAlertIdsByRouteDirectionAndStopCall.clear();
      _serviceAlertIdsByTripId.clear();
      _serviceAlertIdsByTripAndStopId.clear();
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }

    }
  
  @Override
  public Map<AgencyAndId, ServiceAlertRecord> getServiceAlerts() {
    try {
      _available.acquire();
      return _serviceAlerts;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }
  
  @Override
  public ServiceAlertRecord removeServiceAlert(AgencyAndId serviceAlertId) {
    try {
      _available.acquire();
      return _serviceAlerts.remove(serviceAlertId);
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public ServiceAlertRecord putServiceAlert(AgencyAndId id, ServiceAlertRecord serviceAlert) {
    try {
      _available.acquire();
      ServiceAlertRecord existing = _serviceAlerts.get(id);
      _serviceAlerts.put(id, serviceAlert);
      return existing;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }


  @Override
  public Map<String, Set<AgencyAndId>> getServiceAlertIdsByServiceAlertAgencyId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByServiceAlertAgencyId;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<String, Set<AgencyAndId>> getServiceAlertIdsByAgencyId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByAgencyId;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<AgencyAndId, Set<AgencyAndId>> getServiceAlertIdsByStopId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByStopId;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<AgencyAndId, Set<AgencyAndId>> getServiceAlertIdsByRouteId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByRouteId;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<RouteAndDirectionRef, Set<AgencyAndId>> getServiceAlertIdsByRouteAndDirectionId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByRouteAndDirectionId;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<RouteAndStopCallRef, Set<AgencyAndId>> getServiceAlertIdsByRouteAndStop() {
    try {
      _available.acquire();
      return _serviceAlertIdsByRouteAndStop;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<RouteDirectionAndStopCallRef, Set<AgencyAndId>> getServiceAlertIdsByRouteDirectionAndStopCall() {
    try {
      _available.acquire();
      return _serviceAlertIdsByRouteDirectionAndStopCall;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<AgencyAndId, Set<AgencyAndId>> getServiceAlertIdsByTripId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByTripId;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }

  @Override
  public Map<TripAndStopCallRef, Set<AgencyAndId>> getServiceAlertIdsByTripAndStopId() {
    try {
      _available.acquire();
      return _serviceAlertIdsByTripAndStopId;
    } catch (InterruptedException e) {
      // bury
    } finally {
      _available.release();
    }
    return null;
  }


  
}

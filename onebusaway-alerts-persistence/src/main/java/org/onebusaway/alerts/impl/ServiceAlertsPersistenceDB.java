/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ServiceAlertsPersistenceDB implements ServiceAlertsPersistence {
  
  private static final long DEFAULT_REFRESH_INTERVAL = 60 * 1000; // 60 seconds
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsPersistenceDB.class);
  
  private SessionFactory _sessionFactory;
  
  private long lastModified = 0;
  
  private long lastRefresh = 0;
  
  private long rowCount = 0;

  protected long _refreshInterval = DEFAULT_REFRESH_INTERVAL;
  
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }

  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }
  @Override
  @Transactional
  public void delete(ServiceAlertRecord existingServiceAlertRecord) {
    _log.debug("deleting " + (existingServiceAlertRecord==null?"NuLl":existingServiceAlertRecord.getServiceAlertId()));
    getSession().delete(existingServiceAlertRecord);
  }

  
  /**
   * check if our local cache has expired, and if so, sync
   * with persister
   */
  @Transactional(readOnly = true, propagation= Propagation.NOT_SUPPORTED)
  public synchronized boolean cachedNeedsSync() {
    long now = SystemTime.currentTimeMillis();
    
    if (now > lastRefresh + _refreshInterval) {
      lastRefresh = now;
      return needsSync();
    }
    return false;
  }

  @Transactional
  public boolean deleteOrphans() {
    try {
      SQLQuery sqlQuery = getSession().createSQLQuery("delete from transit_data_service_alerts_localized_strings where servicealert_url_id is null AND servicealert_summary_id is null AND servicealert_description_id is null");
      sqlQuery.executeUpdate();
      sqlQuery = getSession().createSQLQuery("delete from transit_data_service_alerts_situation_affects where serviceAlertRecord_Id is null");
      sqlQuery.executeUpdate();
      sqlQuery = getSession().createSQLQuery("delete from transit_data_service_alerts_situation_consequence where serviceAlertRecord_Id is null");
      sqlQuery.executeUpdate();
      sqlQuery = getSession().createSQLQuery("delete from transit_data_service_alerts_time_ranges where serviceAlertRecord_id is null AND servicealert_publication_window_id is null AND servicealert_active_window_id is null");
      sqlQuery.executeUpdate();
    } catch (Exception any) {
      return false;
    }
    return true;
  }

  /**
   *  check if the persister has more recent info then we do, and if so
   *  load it into the cache
   */
  @Transactional(readOnly = true, propagation= Propagation.NOT_SUPPORTED)
  public synchronized boolean needsSync() {
    Long dbLastModified = getLastModified();
    if (dbLastModified != null) {
    } else {
      _log.debug("no dbLastModified with this.lastModified=" + new Date(this.lastModified));
    }
    if (dbLastModified == null) {
      // if the database doesn't have an answer for us, fall back on or refresh interval
      dbLastModified = lastRefresh;
    }
    if (dbLastModified > this.lastModified) {
      _log.debug("in needsSync with delta dbLast - lastmod = "
              + (dbLastModified - this.lastModified)
              + ", "
              + new Date(dbLastModified) + ", " + new Date(this.lastModified)
              + ", "
              + dbLastModified + ", " + lastModified);
      lastModified = dbLastModified;
      return true; // we are out of sync
    }
    
    // check to see if a record was deleted
    long rowCount = getRowCount();
    if (rowCount != this.rowCount) {
      _log.info("rowCount changed from " + this.rowCount + " to " + rowCount);
      this.rowCount = rowCount;
      return true;
    }
    
    return false; // no updates necessary
  }
  
  @Transactional(readOnly = true, propagation= Propagation.NOT_SUPPORTED)
  long getRowCount() {
    try {
        Query query = getSession().createQuery("SELECT count(serviceAlert) FROM ServiceAlertRecord serviceAlert");
        return (Long) query.list().get(0);
    } catch (Throwable t) {
      _log.error("hibernate blew:", t);
      return -1;
    }
  }

  @Override
  @Transactional
  public List<ServiceAlertRecord> getAlerts() {
        Query query = getSession().createQuery("SELECT serviceAlert FROM ServiceAlertRecord serviceAlert " +
                "left join fetch serviceAlert.consequences cs " +
                "left join fetch cs.detourStopIds dsi ");
        return query.list();
  }

  @Override
  @Transactional
  public void saveOrUpdate(ServiceAlertRecord record) {
      getSession().saveOrUpdate(record);
  }

  @Override
  @Transactional
  public void saveOrUpdate(List<ServiceAlertRecord> records) {
    // admittedly this is not ideal but within the transaction hibernate will still batch the statements
    // that is to say this looks worse than it is
    for (ServiceAlertRecord record : records) {
      getSession().saveOrUpdate(record);
    }
  }

  @Override
  @Transactional
  public ServiceAlertRecord getServiceAlertRecordByAlertId(final String agencyId, final String serviceAlertId) {
      Query query = getSession().createQuery("SELECT serviceAlert FROM ServiceAlertRecord serviceAlert WHERE serviceAlertId = :serviceAlertId and agencyId = :agencyId");
      query.setString("serviceAlertId", serviceAlertId);
      query.setString("agencyId", agencyId);
      return (ServiceAlertRecord) query.uniqueResult();
  }

  private Long getLastModified() {
    long start = SystemTime.currentTimeMillis();
    try {
        Query query = getSession().createQuery("SELECT max(serviceAlert.modifiedTime) FROM ServiceAlertRecord serviceAlert");
        return (Long) query.list().get(0);
    } catch (Throwable t) {
      _log.error("hibernate blew:", t);
      return null;
    } finally {
      long stop = SystemTime.currentTimeMillis();
      _log.debug("getLastModified took " + (stop - start) + "ms");
    }
  }

}

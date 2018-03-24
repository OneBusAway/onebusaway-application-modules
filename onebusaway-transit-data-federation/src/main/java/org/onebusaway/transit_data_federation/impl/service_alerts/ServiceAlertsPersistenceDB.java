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
package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServiceAlertsPersistenceDB implements ServiceAlertsPersistence {
  
  private static final long DEFAULT_REFRESH_INTERVAL = 60 * 1000; // 60 seconds
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsPersistenceDB.class);
  
  private HibernateTemplate _template;
  
  private long lastModified = 0;
  
  private long lastRefresh = 0;
  
  private long rowCount = 0;

  protected long _refreshInterval = DEFAULT_REFRESH_INTERVAL;
  
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  public HibernateTemplate getHibernateTemplate() {
    return _template;
  }

  @Override
  public void delete(ServiceAlertRecord existingServiceAlertRecord) {
    _log.info("deleting " + (existingServiceAlertRecord==null?"NuLl":existingServiceAlertRecord.getServiceAlertId()));
    _template.delete(existingServiceAlertRecord);
  }

  
  /**
   * check if our local cache has expired, and if so, sync
   * with persister
   */
  public synchronized boolean cachedNeedsSync() {
    long now = SystemTime.currentTimeMillis();
    
    if (now > lastRefresh + _refreshInterval) {
      lastRefresh = now;
      return needsSync();
    }
    return false;
  }
  
  /**
   *  check if the persister has more recent info then we do, and if so
   *  load it into the cache
   */
  public synchronized boolean needsSync() {
    Long dbLastModified = getLastModified();
    if (dbLastModified == null) {
      // if the database doesn't have an answer for us, fall back on or refresh interval
      dbLastModified = lastRefresh;
    }
    if (dbLastModified > this.lastModified) {
      lastModified = dbLastModified;
      _log.debug("needsSync = true");
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
  
  
  private long getRowCount() {
    try {
      return _template.execute(new HibernateCallback<Long>() {
        @Override
        public Long doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.createQuery("SELECT count(serviceAlert) FROM ServiceAlertRecord serviceAlert");
          return (Long) query.list().get(0);
        }
      });
    } catch (Throwable t) {
      _log.error("hibernate blew:", t);
      return -1;
    }
  }

  @Override
  public List<ServiceAlertRecord> getAlerts() {
    return _template.execute(new HibernateCallback<List<ServiceAlertRecord>>() {
      @SuppressWarnings("unchecked")
      @Override
      public List<ServiceAlertRecord> doInHibernate(Session session)
          throws HibernateException, SQLException {
        Query query = session.createQuery("SELECT serviceAlert FROM ServiceAlertRecord serviceAlert " +
                "left join fetch serviceAlert.consequences cs " +
                "left join fetch cs.detourStopIds dsi ");
        return query.list();
      }
    });
  }

  @Override
  public void saveOrUpdate(ServiceAlertRecord record) {
      _template.saveOrUpdate(record);
  }

  @Override
  public ServiceAlertRecord getServiceAlertRecordByAlertId(final String agencyId, final String serviceAlertId) {
    return _template.execute(new HibernateCallback<ServiceAlertRecord>() {      
      @Override
      public ServiceAlertRecord doInHibernate(Session session)
          throws HibernateException, SQLException {
        Query query = session.createQuery("SELECT serviceAlert FROM ServiceAlertRecord serviceAlert WHERE serviceAlertId = :serviceAlertId and agencyId = :agencyId");
        query.setString("serviceAlertId", serviceAlertId);
        query.setString("agencyId", agencyId);
        return (ServiceAlertRecord) query.uniqueResult();
      }
    });
  }

  private Long getLastModified() {
    long start = SystemTime.currentTimeMillis();
    try {
      return _template.execute(new HibernateCallback<Long>() {
        @Override
        public Long doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.createQuery("SELECT max(serviceAlert.modifiedTime) FROM ServiceAlertRecord serviceAlert");
          return (Long) query.list().get(0);
        }
      });
    } catch (Throwable t) {
      _log.error("hibernate blew:", t);
      return null;
    } finally {
      long stop = SystemTime.currentTimeMillis();
      _log.debug("getLastModified took " + (stop - start) + "ms");
    }
  }

}

/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.Arrays;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.gtfs_realtime.model.AlertModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
/**
 * Database operations specific to the Alert model.
 *
 */
public class AlertDaoImpl implements AlertDao {

  protected static Logger _log = LoggerFactory.getLogger(AlertDaoImpl.class);
  private SessionFactory _sessionFactory;

  @Autowired
//  @Qualifier("gtfsRealtimeArchiveSessionFactory")
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }

  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(AlertModel... array) {
    for (int i = 0; i < array.length; i++) {
      getSession().saveOrUpdate(array[i]);
    }
    getSession().flush();
    getSession().clear();
  }

}

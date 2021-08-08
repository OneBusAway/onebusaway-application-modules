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
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.onebusaway.gtfs_realtime.model.TripUpdateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
/**
 * Database operations specific to the TripUpdate model.
 *
 */
public class TripUpdateDaoImpl implements TripUpdateDao {

  protected static Logger _log = LoggerFactory.getLogger(
      TripUpdateDaoImpl.class);
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
  public void saveOrUpdate(TripUpdateModel... array) {
    for (int i = 0; i< array.length; i++) {
      getSession().saveOrUpdate(array[i]);
    }
    getSession().flush();
    getSession().clear();
  }
  
  @Override
  public List<TripUpdateModel> findByDate(Date startDate, Date endDate) {
    Criteria criteria = getSession().createCriteria(TripUpdateModel.class);
    criteria.add(Restrictions.between("timestamp", startDate, endDate));
    return criteria.list();
  }

}

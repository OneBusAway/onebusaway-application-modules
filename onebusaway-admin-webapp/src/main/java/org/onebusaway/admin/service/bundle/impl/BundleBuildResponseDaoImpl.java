/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.impl;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.service.bundle.BundleBuildResponseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Heretofore BundleBuildResponses were stored by id in a Map. However, whenever
 * the server was restarted the Map was reset and that data was lost. This DAO
 * is for facilitating the persisting of the BundleBuildResponses to a database
 * and provides database operations specific to the BundleBuildResponse model.
 * 
 * @author jpearson
 *
 */
@Repository
public class BundleBuildResponseDaoImpl implements BundleBuildResponseDao {

  protected static Logger _log = LoggerFactory
      .getLogger(BundleBuildResponseDaoImpl.class);
  private SessionFactory _sessionFactory;
  
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }

  @Override
  @Transactional(value="transactionManager", rollbackFor = Throwable.class)
  public void saveOrUpdate(BundleBuildResponse bundleBuildResponse) {
    getSession().saveOrUpdate(bundleBuildResponse);
  }

  @Override
  @Transactional(value="transactionManager", rollbackFor = Throwable.class, readOnly = true)
  public BundleBuildResponse getBundleBuildResponseForId(String id) {
    Session session = getSession();
    List<BundleBuildResponse> responses;
    Query query = session
        .createQuery("from BundleBuildResponse where id=:id");
    query.setParameter("id", id);
    responses = query.list();
    BundleBuildResponse bbr = responses.get(0);
    return bbr;
  }

  @Override
  @Transactional(readOnly = true)
  public int getBundleBuildResponseMaxId() {
    int maxId = 0;
    Session session = getSession();
    List<String> bundleIds = session.createCriteria(BundleBuildResponse.class)
        .setProjection(Projections.property("id")).list();
    for (String bundleId : bundleIds) {
      int thisId = Integer.parseInt(bundleId);
      maxId = (thisId > maxId) ? thisId : maxId;
    }
    
    return maxId;
  }
}

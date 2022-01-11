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
package org.onebusaway.agency_metadata.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
/**
 * Database operations specific to the AgencyMetadata model. 
 *
 */
public class AgencyMetadataDaoImpl implements AgencyMetadataDao {

  protected static Logger _log = LoggerFactory.getLogger(AgencyMetadataDaoImpl.class);
  private SessionFactory _sessionFactory;
  
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }
  
  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(AgencyMetadata metadata) {
    getSession().saveOrUpdate(metadata);
  }

  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(AgencyMetadata... array) {
    for (int i = 0; i<array.length; i++) {
      getSession().saveOrUpdate(array[i]);
    }
    getSession().flush();
    getSession().clear();
  }

  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void delete(long id) {
    List<AgencyMetadata> models;
    Query query = getSession().createQuery("from AgencyMetadata where id=:id");
    query.setParameter("id", Long.valueOf(id));
    models = query.list();
    getSession().delete(models.get(0));
  }

  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void removeAgencyMetadata(String agencyMetadataId) {
    List<AgencyMetadata> models;
    Query query = getSession().createQuery("from AgencyMetadata where id=:id");
    query.setParameter("id", Long.valueOf(agencyMetadataId));
    models = query.list();
    getSession().delete(models.get(0));
  }

  @Transactional(readOnly = true)
  @Override
  public List<AgencyMetadata> getAllAgencyMetadata() {
    return getSession().createQuery("from AgencyMetadata").list();
  }

  @Transactional(readOnly = true)
  @Override
  public List<AgencyMetadata> getAgencyMetadataForId(String id) {
	AgencyMetadata model = (AgencyMetadata) getSession().get(AgencyMetadata.class, Long.valueOf(id));
	List<AgencyMetadata> metadata = new ArrayList<AgencyMetadata>();
	metadata.add(model);
	return metadata;
  }
  
  @Override
  public List<AgencyMetadata> getAgencyMetadataForGtfsId(String gtfsId) {
    Query query = getSession().createQuery("from AgencyMetadata where gtfsId=:gtfsId");
    query.setParameter("gtfsId", gtfsId);
    return query.list();
  }

  @Transactional(readOnly = true)
  @Override
  public List<AgencyMetadata> getAgencyMetadataForName(String name) {
    Query query = getSession().createQuery("from AgencyMetadata where name=:name");
    query.setParameter("name", name);
    return query.list();
  }

  @Transactional(readOnly = true)
  @Override
  public List<AgencyMetadata> getAgencyMetadataForShortName(String shortName) {
    Query query = getSession().createQuery("from AgencyMetadata where shortName=:shortName");
    query.setParameter("shortName", shortName);
    return query.list();
  }

  @Transactional(readOnly = true)
  @Override
  public List<AgencyMetadata> getAgencyMetadataForLegacyId(String legacyId) {
    Query query = getSession().createQuery("from AgencyMetadata where legacyId=:legacyId");
    query.setParameter("legacyId", legacyId);
    return query.list();
  }

  @Transactional(readOnly = true)
  @Override
  public List<AgencyMetadata> getAgencyMetadataForNtdId(String ntdId) {
    Query query = getSession().createQuery("from AgencyMetadata where ntdId=:ntdId");
    query.setParameter("ntdId", ntdId);
    return query.list();
  }

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }

}

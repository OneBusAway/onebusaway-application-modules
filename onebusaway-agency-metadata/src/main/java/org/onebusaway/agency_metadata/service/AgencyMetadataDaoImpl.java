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
import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
/**
 * Database operations specific to the AgencyMetadata model. 
 *
 */
public class AgencyMetadataDaoImpl implements AgencyMetadataDao {

  protected static Logger _log = LoggerFactory.getLogger(AgencyMetadataDaoImpl.class);
  private HibernateTemplate _template;
  
  @Autowired
  @Qualifier("agencyMetadataSessionFactory")
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }
  
  //@Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(AgencyMetadata metadata) {
    _template.saveOrUpdate(metadata);
  }

  //@Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(AgencyMetadata... array) {
    _template.saveOrUpdateAll(Arrays.asList(array));
    _template.flush();
    _template.clear();
  }

  @Override
  public void delete(long id) {
    List<AgencyMetadata> models = _template.findByNamedParam("from AgencyMetadata where id=:id", "id", Long.valueOf(id));
    _template.delete(models.get(0));
  }
  
  @Override
  public List<AgencyMetadata> getAllAgencyMetadata() {
    return _template.find("from AgencyMetadata");
  }
  
  @Override
  public List<AgencyMetadata> getAgencyMetadataForId(String id) {
	AgencyMetadata model = (AgencyMetadata) _template.get(AgencyMetadata.class, Long.valueOf(id));
	List<AgencyMetadata> metadata = new ArrayList<AgencyMetadata>();
	metadata.add(model);
	return metadata;
  }
  
  @Override
  public List<AgencyMetadata> getAgencyMetadataForGtfsId(String gtfsId) {
    return _template.findByNamedParam("from AgencyMetadata where gtfsId=:gtfsId", "gtfsId", gtfsId);
  }
  
  @Override
  public List<AgencyMetadata> getAgencyMetadataForName(String name) {
    return _template.findByNamedParam("from AgencyMetadata where name=:name", "name", name);
  }
  
  @Override
  public List<AgencyMetadata> getAgencyMetadataForShortName(String shortName) {
    return _template.findByNamedParam("from AgencyMetadata where shortName=:shortName", "shortName", shortName);
  }
  
  @Override
  public List<AgencyMetadata> getAgencyMetadataForLegacyId(String legacyId) {
    return _template.findByNamedParam("from AgencyMetadata where legacyId=:legacyId", "legacyId", legacyId);
  }
    
  @Override
  public List<AgencyMetadata> getAgencyMetadataForNtdId(String ntdId) {
    return _template.findByNamedParam("from AgencyMetadata where ntdId=:ntdId", "ntdId", ntdId);
  }
  
}

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

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Projections;
import org.onebusaway.gtfs_realtime.archiver.model.VehiclePositionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

import org.onebusaway.gtfs_realtime.archiver.model.VehiclePositionModel;

public class VehiclePositionDaoImpl implements VehiclePositionDao {

  protected static Logger _log = LoggerFactory.getLogger(VehiclePositionDaoImpl.class);
  private HibernateTemplate _template;
  
  @Autowired
  @Qualifier("gtfsRealtimeArchiveSessionFactory")
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(VehiclePositionModel... array) {
    _template.saveOrUpdateAll(Arrays.asList(array));
    _template.flush();
    _template.clear();
  }
  
  @Override
  public List<String> getAllVehicleIds() {
	  
    // select vehicleId from VehiclePositionModel group by vehicleId
	  
	  DetachedCriteria criteria = DetachedCriteria.forClass(VehiclePositionModel.class)
	      .setProjection(Projections.property("vehicleId"))
	      .setProjection(Projections.groupProperty("vehicleId"));
	  
	  return _template.findByCriteria(criteria);
	  
  }

  public List<VehiclePositionModel> getVehiclePositions(String vehicleId, Date startDate, Date endDate) {
	 
    // from VehiclePositionModel where vehicleId=:vehicleId and timestamp >= :startDate and timestamp < :endDate
    
    DetachedCriteria criteria = DetachedCriteria.forClass(VehiclePositionModel.class)
        .add(Restrictions.eq("vehicleId", vehicleId))
        .add(Restrictions.between("timestamp", startDate, endDate));
    
    return _template.findByCriteria(criteria);
  }
}
 
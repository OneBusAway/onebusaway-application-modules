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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Projections;
import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

public class VehiclePositionDaoImpl implements VehiclePositionDao {

  protected static Logger _log = LoggerFactory.getLogger(
      VehiclePositionDaoImpl.class);
  private SessionFactory _sessionFactory;

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }

  @Autowired
//  @Qualifier("gtfsRealtimeArchiveSessionFactory")
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @Transactional(rollbackFor = Throwable.class)
  @Override
  public void saveOrUpdate(VehiclePositionModel... array) {
    for (int i = 0; i < array.length; i++) {
      getSession().saveOrUpdate(array[i]);
    }
    getSession().flush();
    getSession().clear();
  }

  @Override
  public List<String> getAllVehicleIds() {

    // select distinct vehicleId from VehiclePositionModel

    Projection prop = Projections.distinct(Projections.property("vehicleId"));

    Criteria criteria = getSession().createCriteria(
            VehiclePositionModel.class).setProjection(prop);

    return criteria.list();

  }

  // Get a list of vehicle positions given vehicle ID, start date, and end date
  // If endDate is null choose now
  // If startDate is null choose endDate - 1hr
  @Override
  public List<VehiclePositionModel> getVehiclePositions(String vehicleId,
      Date startDate, Date endDate) {

    if (endDate == null)
      endDate = new Date();

    if (startDate == null) // 1 hr = 3600000 millisec
      startDate = new Date(endDate.getTime() - 3600000);

    // from VehiclePositionModel where vehicleId=:vehicleId and timestamp >=
    // :startDate and timestamp < :endDate order by timestamp

    Criteria criteria = getSession().createCriteria(
            VehiclePositionModel.class);

    if (!StringUtils.isEmpty(vehicleId)) {
      criteria.add(Restrictions.eq("vehicleId", vehicleId));
    }
    criteria.add(Restrictions.between("timestamp", startDate, endDate));
    criteria.addOrder(Order.asc("timestamp"));

    return criteria.list();
  }
  
  @Override
  public List<VehiclePositionModel> findByDate(Date startDate, Date endDate) {
    return getVehiclePositions(null, startDate, endDate);
  }
}

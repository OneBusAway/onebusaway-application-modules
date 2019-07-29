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
package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * Implementation class for {@link BlockLocationRecordDao} that manages
 * persisting {@link BlockLocationRecord} records to a Hibernate-managed
 * data-store.
 * 
 * @author bdferris
 * @see BlockLocationRecordDao
 * @see BlockLocationRecord
 */
@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime:name=BlockLocationRecordDaoImpl")
public class BlockLocationRecordDaoImpl implements BlockLocationRecordDao {

  private SessionFactory _sessionFactory;

  private AtomicInteger _savedRecordCount = new AtomicInteger();

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @ManagedAttribute
  public int getSavedRecordCount() {
    return _savedRecordCount.intValue();
  }

  /****
   * {@link BlockLocationRecordDao} Interface
   ****/

  @Override
  public void saveBlockLocationRecord(BlockLocationRecord record) {
    getSession().save(record);
    _savedRecordCount.incrementAndGet();
  }

  @Override
  public void saveBlockLocationRecords(List<BlockLocationRecord> records) {
    Session session = getSession();
    for (Iterator<BlockLocationRecord> it = records.iterator(); it.hasNext();) {
      session.saveOrUpdate(it.next());
    }
    _savedRecordCount.addAndGet(records.size());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BlockLocationRecord> getBlockLocationRecordsForBlockServiceDateAndTimeRange(
      AgencyAndId blockId, long serviceDate, long fromTime, long toTime) {
    Query query = getSession().getNamedQuery("blockLocationRecordsForBlockServiceDateAndTimeRange");
    query.setParameter("blockId", blockId);
    query.setParameter("serviceDate", serviceDate);
    query.setParameter("fromTime", fromTime);
    query.setParameter("toTime", toTime);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BlockLocationRecord> getBlockLocationRecordsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime) {
    Query query = getSession().getNamedQuery("blockLocationRecordsForVehicleAndTimeRange");
    query.setParameter("vehicleId", vehicleId);
    query.setParameter("fromTime", fromTime);
    query.setParameter("toTime", toTime);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BlockLocationRecord> getBlockLocationRecords(
      final AgencyAndId blockId, final AgencyAndId tripId,
      final AgencyAndId vehicleId, final long serviceDate, final long fromTime,
      final long toTime, final int recordLimit) {

      Criteria c = getSession().createCriteria(BlockLocationRecord.class);

      if (blockId != null)
        c.add(Property.forName("blockId").eq(blockId));
      if (tripId != null)
        c.add(Property.forName("tripId").eq(tripId));
      if (vehicleId != null)
        c.add(Property.forName("vehicleId").eq(vehicleId));
      if (serviceDate != 0)
        c.add(Property.forName("serviceDate").eq(serviceDate));
      if (fromTime != 0)
        c.add(Property.forName("time").ge(fromTime));
      if (toTime != 0)
        c.add(Property.forName("time").le(toTime));
      if (recordLimit != 0)
        c.setFetchSize(recordLimit);

      c.addOrder(Order.asc("time"));

      return c.list();
  }

  private Session getSession(){
    return _sessionFactory.getCurrentSession();
  }
}

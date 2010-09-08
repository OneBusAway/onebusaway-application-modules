package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.orm.hibernate3.HibernateTemplate;
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

  private HibernateTemplate _template;

  private AtomicInteger _savedRecordCount = new AtomicInteger();

  /**
   * Note we are requesting the "mutable" {@link SessionFactory}, aka the one we
   * can write to
   * 
   * @param sessionFactory
   */
  @Autowired
  public void setSessionFactory(
      @Qualifier("mutable") SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
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
    _template.save(record);
    _savedRecordCount.incrementAndGet();
  }

  @Override
  public void saveBlockLocationRecords(List<BlockLocationRecord> records) {
    _template.saveOrUpdateAll(records);
    _savedRecordCount.addAndGet(records.size());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BlockLocationRecord> getBlockLocationRecordsForTripServiceDateAndTimeRange(
      AgencyAndId blockId, long serviceDate, long fromTime, long toTime) {
    String[] paramNames = {"blockId", "serviceDate", "fromTime", "toTime"};
    Object[] paramValues = {blockId, serviceDate, fromTime, toTime};
    return _template.findByNamedQueryAndNamedParam(
        "blockLocationRecordsForBlockServiceDateAndTimeRange", paramNames,
        paramValues);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BlockLocationRecord> getBlockLocationRecordsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime) {
    String[] paramNames = {"vehicleId", "fromTime", "toTime"};
    Object[] paramValues = {vehicleId, fromTime, toTime};
    return _template.findByNamedQueryAndNamedParam(
        "blockLocationRecordsForVehicleAndTimeRange", paramNames, paramValues);
  }

}

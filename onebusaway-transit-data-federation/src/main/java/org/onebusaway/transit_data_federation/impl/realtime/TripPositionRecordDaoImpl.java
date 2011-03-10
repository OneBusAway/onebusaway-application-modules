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
 * Implemenation class for {@link TripPositionRecordDao} that manages persisting
 * {@link TripPositionRecord} records to a Hibernate-managed data-store.
 * 
 * @author bdferris
 * @see TripPositionRecordDao
 * @see TripPositionRecord
 */
@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime:name=TripPositionRecordDaoImpl")
public class TripPositionRecordDaoImpl implements TripPositionRecordDao {

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
   * {@link TripPositionRecordDao} Interface
   ****/

  @Override
  public void saveTripPositionRecord(TripPositionRecord record) {
    _template.save(record);
    _savedRecordCount.incrementAndGet();
  }

  @Override
  public void saveTripPositionRecords(List<TripPositionRecord> records) {
    _template.saveOrUpdateAll(records);
    _savedRecordCount.addAndGet(records.size());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<TripPositionRecord> getTripPositionRecordsForTripServiceDateAndTimeRange(
      AgencyAndId tripId, long serviceDate, long fromTime, long toTime) {
    String[] paramNames = {"tripId", "serviceDate", "fromTime", "toTime"};
    Object[] paramValues = {tripId, serviceDate, fromTime, toTime};
    return _template.findByNamedQueryAndNamedParam(
        "tripPositionRecordsForTripServiceDateAndTimeRange", paramNames,
        paramValues);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<TripPositionRecord> getTripPositionRecordsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime) {
    String[] paramNames = {"vehicleId", "fromTime", "toTime"};
    Object[] paramValues = {vehicleId, fromTime, toTime};
    return _template.findByNamedQueryAndNamedParam(
        "tripPositionRecordsForVehicleAndTimeRange", paramNames, paramValues);
  }

}

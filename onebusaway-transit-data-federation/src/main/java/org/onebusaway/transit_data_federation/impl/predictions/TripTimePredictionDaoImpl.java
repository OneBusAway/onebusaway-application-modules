package org.onebusaway.transit_data_federation.impl.predictions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.predictions:name=TripTimePredictionDaoImpl")
public class TripTimePredictionDaoImpl implements TripTimePredictionDao {

  private HibernateTemplate _template;

  private AtomicInteger _savedPredictionCount = new AtomicInteger();

  @Autowired
  public void setSessionFactory(
      @Qualifier("mutable") SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @ManagedAttribute
  public int getSavedPredictionCount() {
    return _savedPredictionCount.intValue();
  }

  /****
   * {@link TripTimePredictionDao} Interface
   ****/

  @Override
  public void saveTripTimePrediction(TripTimePrediction prediction) {
    _template.save(prediction);
    _savedPredictionCount.incrementAndGet();
  }
  

  @Override
  public void saveTripTimePredictions(List<TripTimePrediction> queue) {
    _template.saveOrUpdateAll(queue);
    _savedPredictionCount.addAndGet(queue.size());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<TripTimePrediction> getTripTimePredictionsForTripServiceDateAndTimeRange(
      AgencyAndId tripId, long serviceDate, long fromTime, long toTime) {
    String[] paramNames = {"tripId", "serviceDate", "fromTime", "toTime"};
    Object[] paramValues = {tripId, serviceDate, fromTime, toTime};
    return _template.findByNamedQueryAndNamedParam(
        "tripTimePredictionsForTripServiceDateAndTimeRange", paramNames,
        paramValues);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<TripTimePrediction> getTripTimePredictionsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime) {
    String[] paramNames = {"vehicleId", "fromTime", "toTime"};
    Object[] paramValues = {vehicleId, fromTime, toTime};
    return _template.findByNamedQueryAndNamedParam(
        "tripTimePredictionsForVehicleAndTimeRange", paramNames, paramValues);
  }


}

package org.onebusaway.transit_data_federation.impl.realtime.history;

import java.util.List;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScheduleDeviationHistoryDaoImpl implements
    ScheduleDeviationHistoryDao {

  private HibernateTemplate _template;

  /**
   * Note we are requesting the "mutable" {@link SessionFactory}, aka the one we
   * can write to
   * 
   * @param sessionFactory
   */
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @Override
  public void saveScheduleDeviationHistory(ScheduleDeviationHistory record) {
    _template.save(record);
  }

  @Override
  public void saveScheduleDeviationHistory(
      List<ScheduleDeviationHistory> records) {
    _template.saveOrUpdateAll(records);
  }

  @Override
  public ScheduleDeviationHistory getScheduleDeviationHistoryForTripId(
      AgencyAndId tripId) {
    return _template.get(ScheduleDeviationHistory.class, tripId);
  }
}

package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;
import org.onebusaway.kcmetro_tcip.services.KCMetroTcipDao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

class KCMetroTcipDaoImpl implements KCMetroTcipDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @SuppressWarnings("unchecked")
  public List<TimepointToStopMapping> getTimepointToStopMappingsForTrackerTripId(
      AgencyAndId trackerTripId) {
    return _template.findByNamedParam(
        "FROM TimepointToStopMapping mapping WHERE mapping.trackerTripId = :trackerTripId",
        "trackerTripId", trackerTripId);
  }

}

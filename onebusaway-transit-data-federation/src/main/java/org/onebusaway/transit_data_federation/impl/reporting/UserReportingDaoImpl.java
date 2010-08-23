package org.onebusaway.transit_data_federation.impl.reporting;

import org.hibernate.SessionFactory;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
class UserReportingDaoImpl implements UserReportingDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(@Qualifier("mutable") SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  @Override
  public void saveOrUpdate(Object record) {
    _template.saveOrUpdate(record);
  }
}

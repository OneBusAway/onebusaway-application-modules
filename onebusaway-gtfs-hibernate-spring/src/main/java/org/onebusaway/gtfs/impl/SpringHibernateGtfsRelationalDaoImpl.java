package org.onebusaway.gtfs.impl;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringHibernateGtfsRelationalDaoImpl extends
    HibernateGtfsRelationalDaoImpl {

  @Override
  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _ops = new SpringHibernateOperationsImpl(sessionFactory);
  }
}

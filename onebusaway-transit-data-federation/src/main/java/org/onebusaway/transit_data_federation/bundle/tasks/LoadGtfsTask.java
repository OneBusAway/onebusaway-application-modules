package org.onebusaway.transit_data_federation.bundle.tasks;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

public class LoadGtfsTask implements Runnable {

  private ApplicationContext _applicationContext;

  private SessionFactory _sessionFactory;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @Override
  public void run() {
    try {
      AutowireCapableBeanFactory factory = _applicationContext.getAutowireCapableBeanFactory();
      HibernateGtfsRelationalDaoImpl store = factory.createBean(HibernateGtfsRelationalDaoImpl.class);
      store.setSessionFactory(_sessionFactory);
      GtfsReadingSupport.readGtfsIntoStore(_applicationContext, store);
    } catch (Throwable ex) {
      throw new IllegalStateException("error loading gtfs", ex);
    }

  }

}

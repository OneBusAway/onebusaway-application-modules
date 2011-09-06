package org.onebusaway.transit_data_federation.bundle.tasks.load_gtfs;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
      HibernateGtfsRelationalDaoImpl store = new HibernateGtfsRelationalDaoImpl();
      store.setSessionFactory(_sessionFactory);
      GtfsReadingSupport.readGtfsIntoStore(_applicationContext, store);
    } catch (Throwable ex) {
      throw new IllegalStateException("error loading gtfs", ex);
    }

  }

}

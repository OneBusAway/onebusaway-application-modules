package org.onebusaway.offline;

import edu.washington.cs.rse.collections.stats.Counter;

import org.hibernate.SessionFactory;
import org.onebusaway.csv.EntityHandler;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.HibernateEntityStore;
import org.onebusaway.where.model.Timepoint;

import java.util.List;

public class GtfsReaderTask extends GtfsReader {

  private SessionFactory _sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public void run() {

    HibernateEntityStore store = new HibernateEntityStore();
    store.setSessionFactory(_sessionFactory);

    setEntityStore(store);
    addEntityHandler(new EntityCounter());

    List<Class<?>> entityClasses = getEntityClasses();
    entityClasses.add(Timepoint.class);
    
    store.open();
    super.run();
    store.close();
  }

  private static class EntityCounter implements EntityHandler {

    private Counter<String> _counter = new Counter<String>();

    public void handleEntity(Object bean) {
      String name = bean.getClass().getName();
      int index = name.lastIndexOf('.');
      if (index != -1)
        name = name.substring(index + 1);
      increment(name);
    }

    private void increment(String key) {
      _counter.increment(key);
      int c = _counter.getCount(key);
      if (c % 100 == 0)
        System.out.println(key + " = " + c);
    }
  }
}

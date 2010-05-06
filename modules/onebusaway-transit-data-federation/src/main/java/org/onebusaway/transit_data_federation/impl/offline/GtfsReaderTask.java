package org.onebusaway.transit_data_federation.impl.offline;

import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.impl.HibernateEntityStore;
import org.onebusaway.gtfs.serialization.GtfsReader;

import edu.washington.cs.rse.collections.stats.Counter;

import org.hibernate.SessionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GtfsReaderTask extends GtfsReader implements Runnable {

  private SessionFactory _sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @Override
  public void run() {

    try {
      HibernateEntityStore store = new HibernateEntityStore();
      store.setSessionFactory(_sessionFactory);

      setEntityStore(store);
      addEntityHandler(new EntityCounter());

      store.open();
      super.run();
      store.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static class EntityCounter implements EntityHandler {

    private Counter<String> _counter = new Counter<String>();
    
    private Map<String,Long> _startTime = new HashMap<String, Long>();

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
      if (c % 100 == 0) {
        double ellapsedTime = (System.currentTimeMillis() - getStartTimeForKey(key)) / 1000.0;
        System.out.println(key + " = " + c + " rate=" + ((long) (c/ellapsedTime)));
      }
    }
    
    private long getStartTimeForKey(String key) {
      Long value = _startTime.get(key);
      if( value == null) {
        value = System.currentTimeMillis();
        _startTime.put(key,value);
      }
      return value;
    }
  }
}

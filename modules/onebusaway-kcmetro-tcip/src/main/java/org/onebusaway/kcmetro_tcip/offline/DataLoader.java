package org.onebusaway.kcmetro_tcip.offline;

import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.gtfs.csv.CsvEntityReader;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.impl.HibernateEntityStore;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;
import org.onebusaway.kcmetro.serialization.KCMetroEntitySchemaFactory;

import edu.washington.cs.rse.collections.stats.Counter;

import org.hibernate.SessionFactory;

import java.io.IOException;

public class DataLoader extends CsvEntityReader {

  private SessionFactory _sessionFactory;

  private HibernateEntityStore _store;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public void run() {

    try {
      _store = new HibernateEntityStore();
      _store.setSessionFactory(_sessionFactory);

      setEntitySchemaFactory(KCMetroEntitySchemaFactory.createEntitySchemaFactory());
      addEntityHandler(new EntityHandlerImpl());

      _store.open();
      readEntities(TimepointToStopMapping.class);
      _store.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private class EntityHandlerImpl implements EntityHandler {

    private Counter<String> _counter = new Counter<String>();

    public void handleEntity(Object entity) {

      if (entity instanceof IdentityBean)
        _store.save(null, entity);

      String name = entity.getClass().getName();
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

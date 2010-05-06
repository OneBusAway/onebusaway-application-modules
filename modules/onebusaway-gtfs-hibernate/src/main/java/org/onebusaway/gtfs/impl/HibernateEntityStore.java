package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.serialization.GtfsEntityStore;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;

public class HibernateEntityStore implements GtfsEntityStore {

  private static final int BUFFER_SIZE = 1000;

  private SessionFactory _sessionFactory;

  private Session _session;

  private Transaction _tx;

  private int _count = 0;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public void open() {
    _session = _sessionFactory.openSession();
    _tx = _session.beginTransaction();
  }

  public void close() {
    _tx.commit();
    _session.close();
  }

  public Object load(Class<?> entityClass, Serializable id) {
    return _session.load(entityClass, id);
  }

  public void save(GtfsReaderContext context, Object entity) {
    _session.save(entity);
    _count++;

    if (_count >= BUFFER_SIZE)
      flush();
  }

  public void flush() {
    _session.flush();
    _session.clear();
    _count = 0;
  }
}

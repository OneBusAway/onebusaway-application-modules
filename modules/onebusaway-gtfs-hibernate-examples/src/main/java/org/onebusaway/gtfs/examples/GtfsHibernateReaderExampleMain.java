package org.onebusaway.gtfs.examples;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.impl.HibernateEntityStore;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;

import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GtfsHibernateReaderExampleMain {

  public static void main(String[] args) throws IOException {

    if (!(args.length == 1 || args.length == 2)) {
      System.err.println("usage: gtfsPath [application-context.xml]");
      System.exit(-1);
    }

    List<String> paths = new ArrayList<String>();
    paths.add("classpath:org/onebusaway/gtfs/examples/application-context.xml");

    if (args.length == 2)
      paths.add("file:" + args[1]);

    ApplicationContext applicationContext = ContainerLibrary.createContext(paths);
    SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(args[0]));

    HibernateEntityStore store = new HibernateEntityStore();

    store.setSessionFactory(sessionFactory);

    reader.setEntityStore(store);

    store.open();
    reader.run();
    store.close();

    GtfsRelationalDao dao = getBeanOfType(applicationContext,
        GtfsRelationalDao.class);

    Collection<Stop> stops = dao.getAllStops();

    for (Stop stop : stops)
      System.out.println(stop.getName());

    CalendarService calendarService = getBeanOfType(applicationContext,
        CalendarService.class);

    Set<AgencyAndId> serviceIds = calendarService.getServiceIds();

    for (AgencyAndId serviceId : serviceIds) {
      Set<Date> dates = calendarService.getServiceDatesForServiceId(serviceId);
      Date from = null;
      Date to = null;
      for (Date date : dates) {
        from = min(from, date);
        to = max(to, date);
      }

      System.out.println("serviceId=" + serviceId + " from=" + from + " to="
          + to);
    }
  }

  private static Date min(Date a, Date b) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    return a.before(b) ? a : b;
  }

  private static Date max(Date a, Date b) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    return a.after(b) ? a : b;
  }

  @SuppressWarnings("unchecked")
  private static <T> T getBeanOfType(ApplicationContext context, Class<T> type) {

    Map<String, Object> beans = context.getBeansOfType(type);

    if (beans.size() == 0)
      throw new IllegalStateException("no bean of type " + type + " found");
    else if (beans.size() > 1)
      throw new IllegalStateException("multiple beans of type " + type
          + " found");

    return (T) beans.values().iterator().next();
  }
}

package org.onebusaway.gtfs_diff;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.impl.HibernateEntityStore;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_diff.impl.serialization.XStreamGtfsDifferencesSerializationServiceImpl;
import org.onebusaway.gtfs_diff.model.GtfsDifferences;
import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;
import org.onebusaway.gtfs_diff.services.GtfsDifferencesSerializationService;

import edu.washington.cs.rse.collections.stats.Counter;

import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GtfsDiffMain {

  public static void main(String[] args) {

    if (args.length != 3) {
      System.err.println("usage: gtfsPathA gtfsPathB outputPath");
      System.exit(-1);
    }

    GtfsDiffMain gtfsDiffMain = new GtfsDiffMain();
    gtfsDiffMain.setInputPaths(new File(args[0]), new File(args[1]));
    gtfsDiffMain.setOutputPath(new File(args[2]));
    gtfsDiffMain.run();

  }

  private File _inputPathA;

  private File _inputPathB;

  private File _outputPath;

  public void setInputPaths(File pathA, File pathB) {
    _inputPathA = pathA;
    _inputPathB = pathB;
  }

  public void setOutputPath(File path) {
    _outputPath = path;
  }

  public void run() {

    try {

      ApplicationContext context = getApplicationContext();
      GtfsDifferences results = (GtfsDifferences) context.getBean("gtfsDifferences");

      if (false) {
        loadGtfs(context, _inputPathA, results.getModelIdA());
        loadGtfs(context, _inputPathB, results.getModelIdB());
      }

      GtfsDifferenceService differencesService = (GtfsDifferenceService) context.getBean("gtfsDifferenceService");
      differencesService.computeDifferences();

      GtfsDifferencesSerializationService serialization = new XStreamGtfsDifferencesSerializationServiceImpl(
          _outputPath);
      serialization.serializeDifferences(results);

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private ApplicationContext getApplicationContext() {
    List<String> resources = new ArrayList<String>();
    resources.add("classpath:org/onebusaway/gtfs_diff/application-context.xml");

    InputStream in = getClass().getClassLoader().getResourceAsStream(
        "data-sources.xml");

    if (in != null) {
      resources.add("classpath:data-sources.xml");
      try {
        in.close();
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }
    
    return ContainerLibrary.createContext(resources);
  }

  private void loadGtfs(ApplicationContext context, File path,
      String agencyIdPrefix) throws IOException {

    SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");

    HibernateEntityStore store = new HibernateEntityStore();
    store.setSessionFactory(sessionFactory);

    GtfsReaderImpl reader = new GtfsReaderImpl(agencyIdPrefix);

    List<Class<?>> entityClasses = reader.getEntityClasses();
    entityClasses.remove(ShapePoint.class);

    reader.setEntityStore(store);
    reader.setInputLocation(path);
    reader.addEntityHandler(new EntityCounter());

    store.open();
    reader.run();
    reader.close();
    store.close();
  }

  private class GtfsReaderImpl extends GtfsReader {

    private String _agencyIdPrefix;

    public GtfsReaderImpl(String agencyIdPrefix) {
      _agencyIdPrefix = agencyIdPrefix;
    }

    @Override
    protected String getTranslatedAgencyId(String agencyId) {
      agencyId = super.getTranslatedAgencyId(agencyId);
      if (!agencyId.startsWith(_agencyIdPrefix))
        agencyId = _agencyIdPrefix + agencyId;
      return agencyId;
    }
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
      if (c % 1000 == 0)
        System.out.println(key + " = " + c);
    }
  }
}

package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.GtfsServiceBundle;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.offline.GtfsReadingSupport;
import org.onebusaway.transit_data_federation.impl.offline.PreCacheTask;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.tripplanner.offline.StopTransfersTripPlannerGraphTask;
import org.onebusaway.transit_data_federation.services.tripplanner.offline.TripPlannerGraphTask;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class FederatedTransitDataBundleCreator {

  public enum Stages {
    GTFS, ROUTE_COLLECTIONS, ROUTE_SEARCH_INDEX, STOP_SEARCH_INDEX, CALENDAR_SERVICE, WALK_GRAPH, TRIP_GRAPH, STOP_TRANSFERS, NARRATIVES, PRE_CACHE
  }

  private List<File> _contextPaths;

  private File _outputPath;

  private Set<Stages> _stagesToSkip = new HashSet<Stages>();

  public void setContextPaths(List<File> contextPaths) {
    _contextPaths = contextPaths;
  }

  public void setOutputPath(File outputPath) {
    _outputPath = outputPath;
  }

  public void setStageToSkip(Stages stage) {
    _stagesToSkip.add(stage);
  }

  public void run() throws IOException, ClassNotFoundException {

    _outputPath.mkdirs();

    List<String> paths = new ArrayList<String>();
    paths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator.xml");
    for (File contextPath : _contextPaths)
      paths.add("file:" + contextPath);

    if (_stagesToSkip.contains(Stages.GTFS))
      paths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-extra.xml");

    Map<String, BeanDefinition> additionalBeans = getAdditionalBeanDefinitions();

    ConfigurableApplicationContext context = ContainerLibrary.createContext(
        paths, additionalBeans);

    GtfsServiceBundle gtfsBundle = ContainerLibrary.getBeanOfType(context,
        GtfsServiceBundle.class);
    FederatedTransitDataBundle bundle = ContainerLibrary.getBeanOfType(context,
        FederatedTransitDataBundle.class);

    if (wants(Stages.GTFS))
      loadGtfsIntoDatabase(context);

    if (wants(Stages.ROUTE_COLLECTIONS)) {
      Runnable generateRouteCollectionsTask = (Runnable) context.getBean("generateRouteCollectionsTask");
      generateRouteCollectionsTask.run();
    }

    if (wants(Stages.ROUTE_SEARCH_INDEX)) {
      RunnableWithOutputPath generateRouteCollectionsSearchIndex = (RunnableWithOutputPath) context.getBean("generateRouteSearchIndexTask");
      generateRouteCollectionsSearchIndex.setOutputPath(bundle.getRouteSearchIndexPath());
      generateRouteCollectionsSearchIndex.run();
    }

    if (wants(Stages.STOP_SEARCH_INDEX)) {
      RunnableWithOutputPath generateStopSearchIndex = (RunnableWithOutputPath) context.getBean("generateStopSearchIndexTask");
      generateStopSearchIndex.setOutputPath(bundle.getStopSearchIndexPath());
      generateStopSearchIndex.run();
    }

    if (wants(Stages.CALENDAR_SERVICE)) {
      RunnableWithOutputPath task = (RunnableWithOutputPath) context.getBean("calendarServiceDataTask");
      task.setOutputPath(gtfsBundle.getCalendarServiceDataPath());
      task.run();
    }

    if (wants(Stages.WALK_GRAPH)) {
      RunnableWithOutputPath walkPlannerGraphTask = (RunnableWithOutputPath) context.getBean("generateWalkPlannerGraphTask");
      walkPlannerGraphTask.setOutputPath(bundle.getWalkPlannerGraphPath());
      walkPlannerGraphTask.run();
    }

    if (wants(Stages.TRIP_GRAPH)) {
      TripPlannerGraphTask task = (TripPlannerGraphTask) context.getBean("generateTripPlannerGraphTask");
      task.setOutputPath(bundle.getTripPlannerGraphPath());
      task.run();
    }

    if (wants(Stages.STOP_TRANSFERS)) {
      WalkPlannerGraph walkPlannerGraph = ObjectSerializationLibrary.readObject(bundle.getWalkPlannerGraphPath());
      TripPlannerGraph graph = ObjectSerializationLibrary.readObject(bundle.getTripPlannerGraphPath());

      StopTransfersTripPlannerGraphTask task = (StopTransfersTripPlannerGraphTask) context.getBean("generateStopTransfersTripPlannerGraphTask");
      task.setWalkPlannerGraph(walkPlannerGraph);
      task.setTripPlannerGraph(graph);
      task.setOutputPath(bundle.getTripPlannerGraphPath());
      task.run();
    }

    if (wants(Stages.NARRATIVES)) {

      Runnable stopTimeTask = (Runnable) context.getBean("generateNarrativesTask");
      stopTimeTask.run();
    }

    // We don't need this context anymore
    context.stop();
    context.close();
    context = null;
    
    if (wants(Stages.PRE_CACHE)) {

      clearExistingCacheFiles(bundle);
      
      List<String> cacheContextPaths = new ArrayList<String>();
      cacheContextPaths.add("classpath:org/onebusaway/transit_data_federation/application-context-services.xml");
      cacheContextPaths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator.xml");
      for (File contextPath : _contextPaths)
        cacheContextPaths.add("file:" + contextPath);
      cacheContextPaths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-extra.xml");

      ConfigurableApplicationContext cacheContext = ContainerLibrary.createContext(
          cacheContextPaths, additionalBeans);

      PreCacheTask task = new PreCacheTask();
      cacheContext.getAutowireCapableBeanFactory().autowireBean(task);
      task.run();
    }
  }

  private void clearExistingCacheFiles(FederatedTransitDataBundle bundle) {
    
    File cacheDir = bundle.getCachePath();
    
    if( ! cacheDir.exists() )
      return;
    
    File[] files = cacheDir.listFiles();
    if( files == null)
      return;
    
    for( File file : files) {
      if( file.isFile())
        file.delete();
    }
  }

  private Map<String, BeanDefinition> getAdditionalBeanDefinitions() {
    Map<String, BeanDefinition> additionalBeans = new HashMap<String, BeanDefinition>();

    BeanDefinitionBuilder bundlePath = BeanDefinitionBuilder.genericBeanDefinition(File.class);
    bundlePath.addConstructorArgValue(_outputPath.getAbsolutePath());
    additionalBeans.put("bundlePath", bundlePath.getBeanDefinition());

    return additionalBeans;
  }

  private boolean wants(Stages stage) {
    boolean wants = !_stagesToSkip.contains(stage);
    if (wants)
      System.out.println("== " + stage + " =====>");
    return wants;
  }

  private void loadGtfsIntoDatabase(ApplicationContext context)
      throws IOException {
    HibernateGtfsRelationalDaoImpl store = new HibernateGtfsRelationalDaoImpl();
    SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    store.setSessionFactory(sessionFactory);
    GtfsReadingSupport.readGtfsIntoStore(context, store);
  }
}

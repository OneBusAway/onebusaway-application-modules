package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.GtfsServiceBundle;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.impl.offline.GtfsReadingSupport;
import org.onebusaway.transit_data_federation.impl.offline.PreCacheTask;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.tripplanner.offline.StopTransfersTripPlannerGraphTask;
import org.onebusaway.transit_data_federation.services.tripplanner.offline.TripPlannerGraphTask;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The primary method for building a new federated transit data bundle, which is
 * the collection of resources and optimized data structures necessary to power
 * a federated transit data bundle instance. The bundle is constructed from a
 * number of input resources, including {@link GtfsBundle} objects and street
 * network resources.
 * 
 * While this class can be accessed directly, it is mostly designed to be run
 * from the command line using the {@link FederatedTransitDataBundleCreatorMain}
 * runner.
 * 
 * Various stages of the bundle building process can be conditionally enabled
 * and disabled by passing {@link Stages} arguments to methods like
 * {@link #setStageToSkip(Stages)}. All stages are enabled by default.
 * 
 * The build process is configured using Spring and additional context config
 * paths can be specified to add to the Spring container (see
 * {@link #setContextPaths(List)}). The core config is kept in the resource:
 * 
 * {@value #PRIMARY_APPLICATION_CONTEXT_RESOURCE}
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreatorMain
 */
public class FederatedTransitDataBundleCreator {

  private static final String PRIMARY_APPLICATION_CONTEXT_RESOURCE = "classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator.xml";

  public enum Stages {
    /**
     * Process GTFS and load it into the database
     */
    GTFS,
    /**
     * Consolidate multiple {@link Route} instances that all refer to the same
     * semantic route into {@link RouteCollection} objects
     */
    ROUTE_COLLECTIONS,
    /**
     * Generate the search index from {@link RouteCollection} short and long
     * name to route instance
     */
    ROUTE_SEARCH_INDEX,
    /**
     * Generate the search index from {@link Stop} name and code to stop
     * instance
     */
    STOP_SEARCH_INDEX,
    /**
     * Compile {@link ServiceCalendar} and {@link ServiceCalendarDate}
     * information into an optimized {@link CalendarServiceData} data structure
     */
    CALENDAR_SERVICE,
    /**
     * Construct the walk planner graph
     */
    WALK_GRAPH,
    /**
     * Construct the trip planner graph
     */
    TRIP_GRAPH,
    /**
     * Construct the optimized set of transfer points in the transit graph
     */
    STOP_TRANSFERS,
    /**
     * Construct the set of {@link StopNarrative}, {@link TripNarrative}, and
     * {@link StopTimeNarrative} objects.
     */
    NARRATIVES,
    /**
     * Pre-cache many of the expensive to construct responses
     */
    PRE_CACHE
  }

  private List<File> _contextPaths;

  private File _outputPath;

  private Set<Stages> _stagesToSkip = new HashSet<Stages>();

  /**
   * Additional context path that will be added when constructing the Spring
   * container that controls the build process. See
   * {@link ContainerLibrary#createContext(Iterable)}.
   * 
   * @param contextPaths additional Spring context paths to add to the container
   */
  public void setContextPaths(List<File> contextPaths) {
    _contextPaths = contextPaths;
  }

  /**
   * 
   * @param outputPath the output path of the bundle
   */
  public void setOutputPath(File outputPath) {
    _outputPath = outputPath;
  }

  /**
   * The specified stage will be skipped in the bundle build process
   * 
   * @param stage state to skip
   */
  public void setStageToSkip(Stages stage) {
    _stagesToSkip.add(stage);
  }

  /**
   * Build the bundle!
   * 
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void run() throws IOException, ClassNotFoundException {

    _outputPath.mkdirs();

    /**
     * Set the bundlePath system property. Will be used by "${bundle_path}"
     * references in the Spring config
     */
    System.setProperty("bundlePath", _outputPath.getAbsolutePath());

    ConfigurableApplicationContext context = createApplicationContext();

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
      cacheContextPaths.add(PRIMARY_APPLICATION_CONTEXT_RESOURCE);
      for (File contextPath : _contextPaths)
        cacheContextPaths.add("file:" + contextPath);
      cacheContextPaths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-extra.xml");

      ConfigurableApplicationContext cacheContext = ContainerLibrary.createContext(cacheContextPaths);

      PreCacheTask task = new PreCacheTask();
      cacheContext.getAutowireCapableBeanFactory().autowireBean(task);
      task.run();
    }
  }

  /****
   * Private Methods
   ****/

  private ConfigurableApplicationContext createApplicationContext() {
    List<String> paths = new ArrayList<String>();
    paths.add(PRIMARY_APPLICATION_CONTEXT_RESOURCE);
    for (File contextPath : _contextPaths)
      paths.add("file:" + contextPath);

    if (_stagesToSkip.contains(Stages.GTFS))
      paths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-extra.xml");

    ConfigurableApplicationContext context = ContainerLibrary.createContext(paths);
    return context;
  }

  private void clearExistingCacheFiles(FederatedTransitDataBundle bundle) {

    File cacheDir = bundle.getCachePath();

    if (!cacheDir.exists())
      return;

    File[] files = cacheDir.listFiles();
    if (files == null)
      return;

    for (File file : files) {
      if (file.isFile())
        file.delete();
    }
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

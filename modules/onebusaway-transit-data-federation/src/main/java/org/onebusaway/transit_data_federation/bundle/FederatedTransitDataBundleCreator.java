package org.onebusaway.transit_data_federation.bundle;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.GtfsServiceBundle;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.impl.offline.GtfsReaderTask;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.tripplanner.offline.StopTransfersTripPlannerGraphTask;
import org.onebusaway.transit_data_federation.services.tripplanner.offline.TripPlannerGraphTask;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;

import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FederatedTransitDataBundleCreator {

  public enum Stages {
    GTFS, ROUTE_COLLECTIONS, ROUTE_SEARCH_INDEX, STOP_SEARCH_INDEX, CALENDAR_SERVICE, WALK_GRAPH, TRIP_GRAPH, STOP_TRANSFERS, WRITE_BUNDLE
  }

  private ApplicationContext _context;

  private SessionFactory _sessionFactory;

  private File _contextPath;

  private File _outputPath;

  private Set<Stages> _stagesToSkip = new HashSet<Stages>();

  public void setContextPath(File contextPath) {
    _contextPath = contextPath;
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
    paths.add("file:" + _contextPath);

    if (_stagesToSkip.contains(Stages.GTFS))
      paths.add("classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-extra.xml");

    _context = ContainerLibrary.createContext(paths);
    _sessionFactory = (SessionFactory) _context.getBean("sessionFactory");

    GtfsServiceBundle gtfsBundle = new GtfsServiceBundle(_outputPath);
    FederatedTransitDataBundle bundle = new FederatedTransitDataBundle(
        _outputPath);

    if (wants(Stages.GTFS))
      loadGtfsIntoDatabase();

    if (wants(Stages.ROUTE_COLLECTIONS)) {
      Runnable generateRouteCollectionsTask = (Runnable) _context.getBean("generateRouteCollectionsTask");
      generateRouteCollectionsTask.run();
    }

    if (wants(Stages.ROUTE_SEARCH_INDEX)) {
      RunnableWithOutputPath generateRouteCollectionsSearchIndex = (RunnableWithOutputPath) _context.getBean("generateRouteSearchIndexTask");
      generateRouteCollectionsSearchIndex.setOutputPath(bundle.getRouteSearchIndexPath());
      generateRouteCollectionsSearchIndex.run();
    }

    if (wants(Stages.STOP_SEARCH_INDEX)) {
      RunnableWithOutputPath generateStopSearchIndex = (RunnableWithOutputPath) _context.getBean("generateStopSearchIndexTask");
      generateStopSearchIndex.setOutputPath(bundle.getStopSearchIndexPath());
      generateStopSearchIndex.run();
    }

    if (wants(Stages.CALENDAR_SERVICE)) {
      RunnableWithOutputPath task = (RunnableWithOutputPath) _context.getBean("calendarServiceDataTask");
      task.setOutputPath(gtfsBundle.getCalendarServiceDataPath());
      task.run();
    }

    if (wants(Stages.WALK_GRAPH)) {
      RunnableWithOutputPath walkPlannerGraphTask = (RunnableWithOutputPath) _context.getBean("generateWalkPlannerGraphTask");
      walkPlannerGraphTask.setOutputPath(bundle.getWalkPlannerGraphPath());
      walkPlannerGraphTask.run();
    }

    if (wants(Stages.TRIP_GRAPH)) {
      TripPlannerGraphTask task = (TripPlannerGraphTask) _context.getBean("generateTripPlannerGraphTask");
      task.setOutputPath(bundle.getTripPlannerGraphPath());
      task.run();
    }

    if (wants(Stages.STOP_TRANSFERS)) {
      WalkPlannerGraph walkPlannerGraph = ObjectSerializationLibrary.readObject(bundle.getWalkPlannerGraphPath());
      TripPlannerGraph graph = ObjectSerializationLibrary.readObject(bundle.getTripPlannerGraphPath());

      StopTransfersTripPlannerGraphTask task = (StopTransfersTripPlannerGraphTask) _context.getBean("generateStopTransfersTripPlannerGraphTask");
      task.setWalkPlannerGraph(walkPlannerGraph);
      task.setTripPlannerGraph(graph);
      task.setOutputPath(bundle.getTripPlannerGraphPath());
      task.run();
    }
  }

  private boolean wants(Stages stage) {
    boolean wants = !_stagesToSkip.contains(stage);
    if (wants)
      System.out.println("== " + stage + " =====>");
    return wants;
  }

  private void loadGtfsIntoDatabase() throws IOException {

    GtfsBundles gtfsBundles = getGtfsBundles(_context);

    List<Agency> agencies = new ArrayList<Agency>();

    for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {

      System.out.println("gtfs=" + gtfsBundle.getPath());

      GtfsReaderTask reader = new GtfsReaderTask();

      reader.setSessionFactory(_sessionFactory);
      reader.setInputLocation(gtfsBundle.getPath());

      // Pre-load the agencies, since one agency can be mentioned across
      // multiple feeds
      reader.setAgencies(agencies);

      if (gtfsBundle.getDefaultAgencyId() != null)
        reader.setDefaultAgencyId(gtfsBundle.getDefaultAgencyId());

      for (Map.Entry<String, String> entry : gtfsBundle.getAgencyIdMappings().entrySet())
        reader.addAgencyIdMapping(entry.getKey(), entry.getValue());

      reader.run();

      agencies.addAll(reader.getAgencies());
    }
  }

  private static GtfsBundles getGtfsBundles(ApplicationContext context) {

    GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
    if (bundles != null)
      return bundles;

    GtfsBundle bundle = (GtfsBundle) context.getBean("gtfs-bundle");
    if (bundle != null) {
      bundles = new GtfsBundles();
      bundles.getBundles().add(bundle);
      return bundles;
    }

    throw new IllegalStateException(
        "must define either \"gtfs-bundles\" or \"gtfs-bundle\" in config");
  }
}

package org.onebusaway.kcmetro2gtfs;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.kcmetro2gtfs.calendar.CalendarManager;
import org.onebusaway.kcmetro2gtfs.calendar.RouteModificationFactoryBean;
import org.onebusaway.kcmetro2gtfs.calendar.RouteModificationsStrategy;
import org.onebusaway.kcmetro2gtfs.calendar.TripScheduleModificationFactoryBean;
import org.onebusaway.kcmetro2gtfs.calendar.TripScheduleModificationStrategy;
import org.onebusaway.kcmetro2gtfs.handlers.StopHandler;
import org.onebusaway.kcmetro2gtfs.impl.DirectReplacementStopNameStrategyFactory;
import org.onebusaway.kcmetro2gtfs.impl.LayersAndRegionsFromPropertiesFactory;
import org.onebusaway.kcmetro2gtfs.impl.LocationNamingStrategyImpl;
import org.onebusaway.kcmetro2gtfs.impl.ModificationFactory;
import org.onebusaway.kcmetro2gtfs.impl.OrderedPatternStopsModificationFactory;
import org.onebusaway.kcmetro2gtfs.impl.ProjectionServiceImpl;
import org.onebusaway.kcmetro2gtfs.impl.StopNameStrategy;
import org.onebusaway.kcmetro2gtfs.impl.TPIPModificationFactory;
import org.onebusaway.kcmetro2gtfs.impl.TranslationContextListener;
import org.onebusaway.layers.model.LayersAndRegions;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.geospatial.DefaultProjection;
import edu.washington.cs.rse.geospatial.ICoordinateProjection;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MetroKCToGtfsMain {

  private static final String ARG_MODIFICATIONS = "modifications";

  private static final String ARG_STOP_NAMES = "stopNames";

  private static final String ARG_REGIONS = "regions";

  private static final String ARG_CALENDAR_MOD = "calendarModifications";

  private static final String ARG_PROJECTION = "projection";

  private static final String ARG_AGENCY_ID = "agencyId";

  private static final String ARG_AGENCIES = "agencies";

  private static final String ARG_ROUTE_MOD = "routeModifications";

  private static final String ARG_WARNING_OUTPUT = "warningOutput";

  private static CommandLineParser _parser = new GnuParser();

  private Options _options = new Options();

  protected String _projectionName = "nad83:4601 +units=us-ft";

  public static void main(String[] args) {
    MetroKCToGtfsMain m = new MetroKCToGtfsMain();
    m.run(args);
  }

  public MetroKCToGtfsMain() {

    buildOptions(_options);
  }

  /*****************************************************************************
   * {@link Runnable} Interface
   ****************************************************************************/

  public void run(String[] args) {

    if (needsHelp(args)) {
      printHelp();
      System.exit(0);
    }

    try {
      CommandLine cli = _parser.parse(_options, args);
      runApplication(cli);
    } catch (MissingOptionException ex) {
      System.err.println("Missing argument: " + ex.getMessage());
      printHelp();
    } catch (MissingArgumentException ex) {
      System.err.println("Missing argument: " + ex.getMessage());
      printHelp();
    } catch (UnrecognizedOptionException ex) {
      System.err.println("Unknown argument: " + ex.getMessage());
      printHelp();
    } catch (AlreadySelectedException ex) {
      System.err.println("Argument already selected: " + ex.getMessage());
      printHelp();
    } catch (ParseException ex) {
      System.err.println(ex.getMessage());
      printHelp();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /*****************************************************************************
   * Abstract Methods
   ****************************************************************************/

  protected void buildOptions(Options options) {

    options.addOption(ARG_PROJECTION, true, "projection id");
    options.addOption(ARG_CALENDAR_MOD, true, "calendar modifications");
    options.addOption(ARG_REGIONS, true, "layer and regions properties file");
    options.addOption(ARG_STOP_NAMES, true, "stop-name overrides");
    options.addOption(ARG_MODIFICATIONS, true, "data modifications");
    options.addOption(ARG_AGENCIES, true, "agencies");
    options.addOption(ARG_ROUTE_MOD, true, "route modifications");
    options.addOption(ARG_WARNING_OUTPUT,true,"warning output file");
    
    Option agencyIdOption = new Option(ARG_AGENCY_ID, "agency id");
    agencyIdOption.setArgName(ARG_AGENCY_ID);
    agencyIdOption.setArgs(1);
    agencyIdOption.setRequired(true);
    options.addOption(agencyIdOption);
  }

  protected void printHelp(PrintWriter out, Options options) {

  }

  protected void runApplication(CommandLine cli) throws Exception {

    String[] args = cli.getArgs();

    if (args.length != 2) {
      System.err.println("You must specify input and output directories");
      printHelp();
      System.exit(-1);
    }

    TranslationContext context = new TranslationContext();

    configureAgencyId(cli, context);
    configureProjectionService(cli, context);
    configureCalendarManager(cli, context);
    configureModifications(cli, context);
    configureLocationNamingStrategy(cli, context);
    configureStopNameOverrides(cli, context);
    configureAdditionalAgencies(cli, context);
    configureRouteModifications(cli, context);
    configureWarningOutput(cli,context);
    
    MetroKCToGtfsProcessor process = new MetroKCToGtfsProcessor();
    process.setInputDirectory(new File(args[0]));
    process.setOutputDirectory(new File(args[1]));
    process.setContext(context);

    process.run();
  }


  private void configureAgencyId(CommandLine cli, TranslationContext context) {
    context.setAgencyId(cli.getOptionValue(ARG_AGENCY_ID));
  }

  protected void configureProjectionService(CommandLine cli,
      TranslationContext context) {

    if (cli.hasOption(ARG_PROJECTION))
      _projectionName = cli.getOptionValue(ARG_PROJECTION);

    ICoordinateProjection projection = new DefaultProjection(_projectionName);
    ProjectionServiceImpl projectionService = new ProjectionServiceImpl();
    projectionService.setProjection(projection);
    context.setProjection(projectionService);
  }

  protected void configureCalendarManager(CommandLine cli,
      TranslationContext context) throws IOException {

    CalendarManager calendarManager = new CalendarManager();
    if (cli.hasOption(ARG_CALENDAR_MOD)) {
      for (String modificationFile : cli.getOptionValues(ARG_CALENDAR_MOD)) {
        TripScheduleModificationFactoryBean factory = new TripScheduleModificationFactoryBean();
        factory.setPath(new File(modificationFile));
        TripScheduleModificationStrategy modification = factory.createModificationStrategy();
        calendarManager.setModificationStrategy(modification);
      }
    }
    context.setCalendarManager(calendarManager);

  }

  protected void configureLocationNamingStrategy(CommandLine cli,
      TranslationContext context) throws FileNotFoundException, IOException,
      SAXException {

    if (cli.hasOption(ARG_REGIONS)) {
      File file = new File(cli.getOptionValue(ARG_REGIONS));
      FileInputStream is = new FileInputStream(file);
      Properties p = new Properties();
      p.load(is);
      is.close();

      LayersAndRegionsFromPropertiesFactory factory = new LayersAndRegionsFromPropertiesFactory();
      factory.setBaseDirectory(file.getParentFile());
      factory.setProjectionService(context.getProjectionService());
      LayersAndRegions layersAndRegions = factory.create(p);
      LocationNamingStrategyImpl namingStrategy = new LocationNamingStrategyImpl();
      namingStrategy.setLayersAndRegions(layersAndRegions);
      context.setLocationNamingStrategy(namingStrategy);
    }
  }

  protected void configureStopNameOverrides(CommandLine cli,
      TranslationContext context) throws IOException {

    if (!cli.hasOption(ARG_STOP_NAMES))
      return;

    File path = new File(cli.getOptionValue(ARG_STOP_NAMES));

    System.out.println("STOP NAMES=" + path);

    DirectReplacementStopNameStrategyFactory factory = new DirectReplacementStopNameStrategyFactory();
    final StopNameStrategy strategy = factory.create(path);

    context.addContextListener(new TranslationContextListener() {
      public void onHandlerRegistered(Class<?> type, Object handler) {
        if (handler instanceof StopHandler) {
          StopHandler stopHandler = (StopHandler) handler;
          stopHandler.addStopNameStrategy(strategy);
        }
      }
    });
  }

  protected void configureModifications(CommandLine cli,
      TranslationContext context) throws IOException {

    if (cli.hasOption(ARG_MODIFICATIONS)) {

      Map<String, ModificationFactory> factories = new HashMap<String, ModificationFactory>();
      factories.put("orderedPatternStops",
          new OrderedPatternStopsModificationFactory());
      factories.put("tpips", new TPIPModificationFactory());

      Map<String, List<Map<String, String>>> configs = new FactoryMap<String, List<Map<String, String>>>(
          new ArrayList<Map<String, String>>());

      BufferedReader reader = new BufferedReader(new FileReader(
          cli.getOptionValue(ARG_MODIFICATIONS)));
      String line = null;

      while ((line = reader.readLine()) != null) {
        Map<String, String> config = new HashMap<String, String>();
        String[] tokens = line.trim().split("\\s+");
        for (String token : tokens) {
          String[] kvp = token.split("=");
          if (kvp.length != 2)
            throw new IllegalStateException("invalid line=" + line);
          config.put(kvp[0], kvp[1]);
        }

        String type = config.get("type");
        if (type == null)
          throw new IllegalStateException("no modification type: line=" + line);
        configs.get(type).add(config);
      }

      reader.close();

      for (String key : factories.keySet()) {
        ModificationFactory factory = factories.get(key);
        if (configs.containsKey(key)) {
          factory.register(configs.get(key), context);
        }
      }
    }
  }

  private void configureAdditionalAgencies(CommandLine cli,
      TranslationContext context) throws IOException {
    if (cli.hasOption(ARG_AGENCIES)) {
      File agenciesFile = new File(cli.getOptionValue(ARG_AGENCIES));
      GtfsReader reader = new GtfsReader();
      reader.readEntities(Agency.class, new FileInputStream(agenciesFile));
      List<Agency> agencies = reader.getAgencies();
      context.addAgencies(agencies);
    }
  }

  private void configureRouteModifications(CommandLine cli,
      TranslationContext context) throws Exception {
    if (cli.hasOption(ARG_ROUTE_MOD)) {
      String modificationsFile = cli.getOptionValue(ARG_ROUTE_MOD);
      RouteModificationFactoryBean factory = new RouteModificationFactoryBean();
      factory.setResource(new File(modificationsFile));
      RouteModificationsStrategy modifications = factory.createInstance();
      context.setRouteModifications(modifications);
    }
  }
  

  private void configureWarningOutput(CommandLine cli,
      TranslationContext context) {
    
    if(cli.hasOption(ARG_WARNING_OUTPUT) ) {
      File output = new File(cli.getOptionValue(ARG_WARNING_OUTPUT));
      context.setWarningOutputFile(output);
    }
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  protected void printHelp() {
    printHelp(new PrintWriter(System.err, true), _options);
  }

  private boolean needsHelp(String[] args) {
    for (String arg : args) {
      if (arg.equals("-h") || arg.equals("--help"))
        return true;
    }
    return false;
  }
}

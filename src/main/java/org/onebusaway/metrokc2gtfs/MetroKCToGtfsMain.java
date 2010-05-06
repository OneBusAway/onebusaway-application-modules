package org.onebusaway.metrokc2gtfs;

import org.onebusaway.common.impl.ProjectionServiceImpl;
import org.onebusaway.common.model.LayersAndRegions;
import org.onebusaway.metrokc2gtfs.calendar.CalendarManager;
import org.onebusaway.metrokc2gtfs.calendar.TripScheduleModificationFactoryBean;
import org.onebusaway.metrokc2gtfs.calendar.TripScheduleModificationStrategy;
import org.onebusaway.metrokc2gtfs.handlers.StopHandler;
import org.onebusaway.metrokc2gtfs.impl.DirectReplacementStopNameStrategyFactory;
import org.onebusaway.metrokc2gtfs.impl.LayersAndRegionsFromPropertiesFactory;
import org.onebusaway.metrokc2gtfs.impl.LocationNamingStrategyImpl;
import org.onebusaway.metrokc2gtfs.impl.ModificationFactory;
import org.onebusaway.metrokc2gtfs.impl.OrderedPatternStopsModificationFactory;
import org.onebusaway.metrokc2gtfs.impl.StopNameStrategy;
import org.onebusaway.metrokc2gtfs.impl.TPIPModificationFactory;
import org.onebusaway.metrokc2gtfs.impl.TranslationContextListener;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.geospatial.DefaultProjection;
import edu.washington.cs.rse.geospatial.ICoordinateProjection;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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

  private static CommandLineParser _parser = new PosixParser();

  private Options _options = new Options();

  private Options _helpOptions = new Options();

  protected String _projectionName = "nad83:4601 +units=us-ft";

  public static void main(String[] args) {
    MetroKCToGtfsMain m = new MetroKCToGtfsMain();
    m.run(args);
  }

  public MetroKCToGtfsMain() {
    // The Help option
    Option h = new Option("h", "help", false, "print this message");
    h.setOptionalArg(true);
    _helpOptions.addOption(h);

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
    options.addOption("p", "projection", true, "projection id");
    options.addOption("c", "calendar-modification", true, "calendar modifications");
    options.addOption("r", "regions", true, "layer and regions properties file");
    options.addOption("s", "stop-names", true, "stop-name overrides");
    options.addOption("m", "modifications", true, "data modifications");
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

    configureProjectionService(cli, context);
    configureCalendarManager(cli, context);
    configureModifications(cli, context);
    configureLocationNamingStrategy(cli, context);
    configureStopNameOverrides(cli, context);

    MetroKCToGtfsProcessor process = new MetroKCToGtfsProcessor();
    process.setInputDirectory(new File(args[0]));
    process.setOutputDirectory(new File(args[1]));
    process.setContext(context);

    process.run();
  }

  protected void configureProjectionService(CommandLine cli, TranslationContext context) {

    if (cli.hasOption('p'))
      _projectionName = cli.getOptionValue('p');

    ICoordinateProjection projection = new DefaultProjection(_projectionName);
    ProjectionServiceImpl projectionService = new ProjectionServiceImpl();
    projectionService.setProjection(projection);
    context.setProjection(projectionService);
  }

  protected void configureCalendarManager(CommandLine cli, TranslationContext context) throws IOException {

    CalendarManager calendarManager = new CalendarManager();
    if (cli.hasOption('c')) {
      for (String modificationFile : cli.getOptionValues('c')) {
        TripScheduleModificationFactoryBean factory = new TripScheduleModificationFactoryBean();
        factory.setPath(new File(modificationFile));
        TripScheduleModificationStrategy modification = factory.createModificationStrategy();
        calendarManager.setModificationStrategy(modification);
      }
    }
    context.setCalendarManager(calendarManager);

  }

  protected void configureLocationNamingStrategy(CommandLine cli, TranslationContext context)
      throws FileNotFoundException, IOException, SAXException {

    if (cli.hasOption('r')) {
      FileInputStream is = new FileInputStream(cli.getOptionValue('r'));
      Properties p = new Properties();
      p.load(is);
      is.close();

      LayersAndRegionsFromPropertiesFactory factory = new LayersAndRegionsFromPropertiesFactory();
      factory.setProjectionService(context.getProjectionService());
      LayersAndRegions layersAndRegions = factory.create(p);
      LocationNamingStrategyImpl namingStrategy = new LocationNamingStrategyImpl();
      namingStrategy.setLayersAndRegions(layersAndRegions);
      context.setLocationNamingStrategy(namingStrategy);
    }
  }

  protected void configureStopNameOverrides(CommandLine cli, TranslationContext context) throws IOException {

    if (!cli.hasOption('s'))
      return;

    File path = new File(cli.getOptionValue('s'));
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

  protected void configureModifications(CommandLine cli, TranslationContext context) throws IOException {

    if (cli.hasOption('m')) {

      Map<String, ModificationFactory> factories = new HashMap<String, ModificationFactory>();
      factories.put("orderedPatternStops", new OrderedPatternStopsModificationFactory());
      factories.put("tpips", new TPIPModificationFactory());

      Map<String, List<Map<String, String>>> configs = new FactoryMap<String, List<Map<String, String>>>(
          new ArrayList<Map<String, String>>());

      BufferedReader reader = new BufferedReader(new FileReader(cli.getOptionValue('m')));
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

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  protected void printHelp() {
    printHelp(new PrintWriter(System.err, true), _options);
  }

  private boolean needsHelp(String[] args) {
    try {
      CommandLine cl = _parser.parse(_helpOptions, args);
      return cl.hasOption('h');
    } catch (ParseException e) {
      System.err.println("Error parsing command line options: " + e.getMessage());
      return true;
    }
  }
}

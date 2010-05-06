/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.kcmetro2gtfs;

import org.onebusaway.gtfs.csv.CSVLibrary;
import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.CsvEntityContextImpl;
import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.csv.IndividualCsvEntityReader;
import org.onebusaway.gtfs.csv.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.kcmetro.serialization.KCMetroEntitySchemaFactory;
import org.onebusaway.kcmetro2gtfs.calendar.CalendarManager;
import org.onebusaway.kcmetro2gtfs.handlers.AgencyHandler;
import org.onebusaway.kcmetro2gtfs.handlers.BlockTripHandler;
import org.onebusaway.kcmetro2gtfs.handlers.ChangeDateHandler;
import org.onebusaway.kcmetro2gtfs.handlers.InputHandler;
import org.onebusaway.kcmetro2gtfs.handlers.OrderedPatternStopsHandler;
import org.onebusaway.kcmetro2gtfs.handlers.PatternTimepointsHandler;
import org.onebusaway.kcmetro2gtfs.handlers.RouteNameHandler;
import org.onebusaway.kcmetro2gtfs.handlers.RoutesHandler;
import org.onebusaway.kcmetro2gtfs.handlers.ServicePatternHandler;
import org.onebusaway.kcmetro2gtfs.handlers.ShapePointHandler;
import org.onebusaway.kcmetro2gtfs.handlers.StopHandler;
import org.onebusaway.kcmetro2gtfs.handlers.StopTimeHandler;
import org.onebusaway.kcmetro2gtfs.handlers.StopTimeInterpolationHandler;
import org.onebusaway.kcmetro2gtfs.handlers.StreetNameHandler;
import org.onebusaway.kcmetro2gtfs.handlers.TPIPathHandler;
import org.onebusaway.kcmetro2gtfs.handlers.TransLinkHandler;
import org.onebusaway.kcmetro2gtfs.handlers.TransNodeHandler;
import org.onebusaway.kcmetro2gtfs.handlers.TripHandler;
import org.onebusaway.kcmetro2gtfs.handlers.TripNameHandler;
import org.onebusaway.kcmetro2gtfs.impl.MetroDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class MetroKCToGtfsProcessor {

  /*****************************************************************************
   * 
   ****************************************************************************/

  private File _inputDirectory;

  private File _outputDirectory;

  private TranslationContext _context;

  private EntitySchemaFactory _schemaFactory;

  public void setInputDirectory(File inputDirectory) {
    _inputDirectory = inputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    _outputDirectory = outputDirectory;
  }

  public void setContext(TranslationContext context) {
    _context = context;
  }

  public void run() throws Exception {

    System.out.println("Loading Input Directory=" + _inputDirectory);

    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    schemaFactory.addFactory(GtfsEntitySchemaFactory.createEntitySchemaFactory());
    schemaFactory.addFactory(KCMetroEntitySchemaFactory.createEntitySchemaFactory());

    AnnotationDrivenEntitySchemaFactory entitySchemaFactory = new AnnotationDrivenEntitySchemaFactory();
    entitySchemaFactory.addPackageToScan("org.onebusaway.kcmetro2gtfs.model");
    schemaFactory.addFactory(entitySchemaFactory);

    _schemaFactory = schemaFactory;

    _context.setDao(new MetroDao(_context));
    _context.setReaderContext(new CsvEntityContextImpl());

    CsvEntityWriter writer = new CsvEntityWriter();
    writer.setOutputLocation(_outputDirectory);
    writer.setEntitySchemaFactory(_schemaFactory);
    _context.setWriter(writer);

    handleEntity(new AgencyHandler(_context));
    handleEntity(new ChangeDateHandler());
    RoutesHandler routesHandler = handleEntity(new RoutesHandler(_context));

    handleEntity(new ServicePatternHandler(_context));
    handleEntity(new StreetNameHandler());
    handleEntity(new TransLinkHandler());
    handleEntity(new BlockTripHandler(_context));
    handleEntity(new PatternTimepointsHandler(_context));
    handleEntity(new TPIPathHandler(_context));

    handleEntity(new OrderedPatternStopsHandler(_context));
    StopHandler stopHandler = handleEntity(new StopHandler(_context));

    ShapePointHandler shapeHandler = handleEntity(new ShapePointHandler(
        _context));

    handleEntity(new TransNodeHandler());
    handleEntity(new StopTimeHandler(_context));
    handleRunnable(new StopTimeInterpolationHandler(_context));
    TripHandler trips = handleEntity(new TripHandler(_context));

    handleRunnable(new TripNameHandler(_context));

    trips.writeTrips();

    stopHandler.writeResults(trips.getActiveStops());
    shapeHandler.writeShapes();

    handleRunnable(new RouteNameHandler(_context));
    routesHandler.writeRoutes();

    CalendarManager calendarManager = _context.getCalendarManager();
    calendarManager.writeCalendars(_context);

    writer.close();

    List<String> warnings = _context.getWarnings();
    Collections.sort(warnings);
    if (!warnings.isEmpty()) {
      File outputFile = _context.getWarningOutputFile();
      PrintStream out = System.err;
      if (outputFile != null)
        out = new PrintStream(new FileOutputStream(outputFile));
      out.println("==== WARNINGS ====");
      for (String warning : warnings)
        out.println(warning);
      out.close();
    }
  }

  private <T extends Runnable> T handleRunnable(T handler) {
    System.out.println("====> " + handler.getClass().getName());
    _context.putHandler(handler);
    handler.run();
    return handler;
  }

  private <T extends InputHandler> T handleEntity(T handler) throws Exception {
    _context.putHandler(handler);
    return handleEntity(handler, _schemaFactory, _context, _inputDirectory);
  }

  public static <T extends InputHandler> T handleEntity(T handler,
      EntitySchemaFactory schemaFactory, TranslationContext context,
      File inputDirectory) throws Exception {

    CsvEntityContext readerContext = context.getReaderContext();

    Class<?> entityType = handler.getEntityType();
    EntitySchema schema = schemaFactory.getSchema(entityType);
    List<String> fields = handler.getEntityFields();
    String filename = schema.getFilename();

    if (handler.getFilename() != null)
      filename = handler.getFilename();

    System.out.println("====> " + entityType.getName());

    IndividualCsvEntityReader entityLoader = new IndividualCsvEntityReader(
        handler, readerContext, schema, fields);
    entityLoader.setVerbose(true);
    File inputFile = new File(inputDirectory, filename);

    handler.open();

    CSVLibrary.parse(inputFile, entityLoader);
    handler.close();

    if (context.getWriter() != null)
      context.getWriter().flush();

    return handler;
  }
}

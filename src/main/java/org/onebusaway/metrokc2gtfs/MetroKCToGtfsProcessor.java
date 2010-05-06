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
package org.onebusaway.metrokc2gtfs;

import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.CsvEntityContextImpl;
import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.csv.EntitySchema;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.IndividualCsvEntityReader;
import org.onebusaway.metrokc2gtfs.calendar.CalendarManager;
import org.onebusaway.metrokc2gtfs.handlers.AgencyHandler;
import org.onebusaway.metrokc2gtfs.handlers.BlockTripHandler;
import org.onebusaway.metrokc2gtfs.handlers.ChangeDateHandler;
import org.onebusaway.metrokc2gtfs.handlers.InputHandler;
import org.onebusaway.metrokc2gtfs.handlers.OrderedPatternStopsHandler;
import org.onebusaway.metrokc2gtfs.handlers.PatternTimepointsHandler;
import org.onebusaway.metrokc2gtfs.handlers.RouteNameHandler;
import org.onebusaway.metrokc2gtfs.handlers.RoutesHandler;
import org.onebusaway.metrokc2gtfs.handlers.ServicePatternHandler;
import org.onebusaway.metrokc2gtfs.handlers.ShapePointHandler;
import org.onebusaway.metrokc2gtfs.handlers.StopHandler;
import org.onebusaway.metrokc2gtfs.handlers.StopTimeHandler;
import org.onebusaway.metrokc2gtfs.handlers.StopTimeInterpolationHandler;
import org.onebusaway.metrokc2gtfs.handlers.StreetNameHandler;
import org.onebusaway.metrokc2gtfs.handlers.TPIPathHandler;
import org.onebusaway.metrokc2gtfs.handlers.TransLinkHandler;
import org.onebusaway.metrokc2gtfs.handlers.TransNodeHandler;
import org.onebusaway.metrokc2gtfs.handlers.TripHandler;
import org.onebusaway.metrokc2gtfs.handlers.TripNameHandler;
import org.onebusaway.metrokc2gtfs.impl.MetroDao;

import edu.washington.cs.rse.text.CSVLibrary;

import java.io.File;
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

    _schemaFactory = new EntitySchemaFactory();

    _context.setDao(new MetroDao(_context));
    _context.setReaderContext(new CsvEntityContextImpl());

    CsvEntityWriter writer = new CsvEntityWriter();
    writer.setOutputLocation(_outputDirectory);
    _context.setWriter(writer);

    handleEntity(new AgencyHandler(_context));
    handleEntity(new ChangeDateHandler());
    RoutesHandler routesHandler = handleEntity(new RoutesHandler(_context));

    handleEntity(new ServicePatternHandler(_context));
    handleEntity(new StreetNameHandler());
    handleEntity(new TransLinkHandler());
    handleEntity(new BlockTripHandler());
    handleEntity(new PatternTimepointsHandler(_context));
    handleEntity(new TPIPathHandler(_context));

    OrderedPatternStopsHandler opsHandler = handleEntity(new OrderedPatternStopsHandler());
    StopHandler stopHandler = handleEntity(new StopHandler(_context));

    ShapePointHandler shapeHandler = handleEntity(new ShapePointHandler(_context));
    shapeHandler.writeShapes();

    handleEntity(new TransNodeHandler());
    handleEntity(new StopTimeHandler());
    handleRunnable(new StopTimeInterpolationHandler(_context));
    TripHandler trips = handleEntity(new TripHandler(_context));

    handleRunnable(new TripNameHandler(_context));
    stopHandler.writeResults(opsHandler.getActiveStops());
    trips.writeTrips();

    handleRunnable(new RouteNameHandler(_context));
    routesHandler.writeRoutes();

    CalendarManager calendarManager = _context.getCalendarManager();
    calendarManager.writeCalendars(_context);

    writer.close();

    List<String> warnings = _context.getWarnings();
    Collections.sort(warnings);
    if (!warnings.isEmpty()) {
      System.out.println("==== WARNINGS ====");
      for (String warning : warnings)
        System.out.println(warning);
    }
  }

  private <T extends Runnable> T handleRunnable(T handler) {
    System.out.println("====> " + handler.getClass().getName());
    _context.putHandler(handler);
    handler.run();
    return handler;
  }

  private <T extends InputHandler> T handleEntity(T handler) throws Exception {

    Class<?> entityType = handler.getEntityType();
    EntitySchema schema = _schemaFactory.getSchema(entityType);
    List<String> fields = handler.getEntityFields();
    String filename = schema.getFilename();

    if (handler.getFilename() != null)
      filename = handler.getFilename();

    System.out.println("====> " + entityType.getName());
    _context.putHandler(handler);

    CsvEntityContext readerContext = _context.getReaderContext();
    IndividualCsvEntityReader entityLoader = new IndividualCsvEntityReader(handler, readerContext, schema, fields);
    entityLoader.setVerbose(true);
    File inputFile = new File(_inputDirectory, filename);

    handler.open();

    CSVLibrary.parse(inputFile, entityLoader);
    handler.close();

    return handler;
  }
}

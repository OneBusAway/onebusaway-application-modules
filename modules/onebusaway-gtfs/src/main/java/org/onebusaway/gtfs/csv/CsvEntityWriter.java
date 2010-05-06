package org.onebusaway.gtfs.csv;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class CsvEntityWriter implements EntityHandler {

  private File _outputLocation;

  private EntitySchemaFactory _entitySchemaFactory = new DefaultEntitySchemaFactory();

  private CsvEntityContext _context = new CsvEntityContextImpl();

  private Map<Class<?>, IndividualCsvEntityWriter> _writersByType = new HashMap<Class<?>, IndividualCsvEntityWriter>();

  public void setEntitySchemaFactory(EntitySchemaFactory entitySchemaFactory) {
    _entitySchemaFactory = entitySchemaFactory;
  }

  public void setOutputLocation(File path) {
    _outputLocation = path;
  }

  public void handleEntity(Object entity) {
    Class<?> entityType = entity.getClass();
    IndividualCsvEntityWriter writer = getEntityWriter(entityType);
    writer.handleEntity(entity);
  }

  public void flush() {
    for (IndividualCsvEntityWriter writer : _writersByType.values())
      writer.flush();
  }
  
  public void close() {
    for (IndividualCsvEntityWriter writer : _writersByType.values())
      writer.close();
  }

  private IndividualCsvEntityWriter getEntityWriter(Class<?> entityType) {

    IndividualCsvEntityWriter entityWriter = _writersByType.get(entityType);
    if (entityWriter == null) {
      EntitySchema schema = _entitySchemaFactory.getSchema(entityType);
      File outputFile = new File(_outputLocation, schema.getFilename());

      if (!_outputLocation.exists())
        _outputLocation.mkdirs();

      PrintWriter writer = openOutput(outputFile);
      entityWriter = new IndividualCsvEntityWriter(_context, schema, writer);
      _writersByType.put(entityType, entityWriter);
    }
    return entityWriter;
  }

  private PrintWriter openOutput(File outputFile) {
    try {
      return new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
    } catch (IOException ex) {
      throw new IllegalStateException("error opening output file: "
          + outputFile, ex);
    }
  }
}

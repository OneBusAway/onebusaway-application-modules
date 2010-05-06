package org.onebusaway.gtfs.csv;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchema;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class CsvEntityReader {

  public static final String KEY_CONTEXT = CsvEntityReader.class.getName()
      + ".context";

  private EntitySchemaFactory _entitySchemaFactory = new DefaultEntitySchemaFactory();

  private EntityHandlerImpl _handler = new EntityHandlerImpl();

  private CsvEntityContextImpl _context = new CsvEntityContextImpl();

  private CsvInputSource _source;

  private List<EntityHandler> _handlers = new ArrayList<EntityHandler>();

  private boolean _trimValues = false;

  public void setEntitySchemaFactory(EntitySchemaFactory entitySchemaFactory) {
    _entitySchemaFactory = entitySchemaFactory;
  }

  public void setInputSource(CsvInputSource source) {
    _source = source;
  }

  public void setInputLocation(File path) throws IOException {
    if (path.isDirectory())
      _source = new FileCsvInputSource(path);
    else
      _source = new ZipFileCsvInputSource(new ZipFile(path));
  }

  public void setTrimValues(boolean trimValues) {
    _trimValues = trimValues;
  }

  public void addEntityHandler(EntityHandler handler) {
    _handlers.add(handler);
  }

  public CsvEntityContext getContext() {
    return _context;
  }

  public void readEntities(Class<?> entityClass) throws IOException {
    
    EntitySchema schema = _entitySchemaFactory.getSchema(entityClass);

    String name = schema.getFilename();
    if (!_source.hasResource(name)) {
      if (schema.isRequired())
        throw new IllegalStateException("entity input " + name
            + " is required, but not present");
      return;
    }

    InputStream is = _source.getResource(name);
    readEntities(entityClass, is);
  }

  public void readEntities(Class<?> entityClass, InputStream is)
      throws IOException {

    EntitySchema schema = _entitySchemaFactory.getSchema(entityClass);

    try {

      IndividualCsvEntityReader entityLoader = new IndividualCsvEntityReader(
          _context, schema, _handler);
      entityLoader.setTrimValues(_trimValues);
      CSVLibrary.parse(is, entityLoader);
      is.close();

    } catch (Exception ex) {
      throw new IllegalStateException("error loading entities of type "
          + entityClass, ex);
    }
  }

  public void close() throws IOException {
    _source.close();
  }

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {
      for (EntityHandler handler : _handlers)
        handler.handleEntity(entity);
    }
  }
}

package org.onebusaway.csv;

import edu.washington.cs.rse.text.CSVLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class CsvEntityReader {

  public static final String KEY_CONTEXT = CsvEntityReader.class.getName()
      + ".context";

  private EntitySchemaFactory _entitySchemaFactory = new EntitySchemaFactory();

  private EntityHandlerImpl _handler = new EntityHandlerImpl();

  private CsvEntityContextImpl _context = new CsvEntityContextImpl();

  private File _inputLocation;

  private List<EntityHandler> _handlers = new ArrayList<EntityHandler>();

  public void setInputLocation(File path) {
    _inputLocation = path;
  }

  public void addEntityHandler(EntityHandler handler) {
    _handlers.add(handler);
  }

  public CsvEntityContext getContext() {
    return _context;
  }

  public void readEntities(Class<?> entityClass) {

    EntitySchema schema = _entitySchemaFactory.getSchema(entityClass);

    try {

      IndividualCsvEntityReader entityLoader = new IndividualCsvEntityReader(
          _context, schema, _handler);
      InputStream is = getInput(schema.getFilename());
      CSVLibrary.parse(is, entityLoader);
      is.close();

    } catch (Exception ex) {
      throw new IllegalStateException("error loading entities from "
          + schema.getFilename(), ex);
    }
  }

  private InputStream getInput(String name) throws ZipException, IOException {

    if (_inputLocation.isDirectory()) {
      File path = new File(_inputLocation, name);
      return new FileInputStream(path);
    }

    ZipFile zFile = new ZipFile(_inputLocation);
    ZipEntry zEntry = zFile.getEntry(name);
    return zFile.getInputStream(zEntry);
  }

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {
      for (EntityHandler handler : _handlers)
        handler.handleEntity(entity);
    }
  }
}

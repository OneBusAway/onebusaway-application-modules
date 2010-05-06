package org.onebusaway.metrokc2gtfs.handlers;

import org.onebusaway.csv.EntityHandler;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.metrokc2gtfs.TranslationContext;

import java.util.ArrayList;
import java.util.List;

public abstract class InputHandler implements EntityHandler {

  private Class<?> _entityType;

  private List<String> _entityFields = new ArrayList<String>();

  private String _filename;

  public InputHandler(Class<?> entityType, String[] fieldNames) {

    _entityType = entityType;

    for (String fieldName : fieldNames)
      _entityFields.add(fieldName);
  }

  public InputHandler(TranslationContext context, Class<Agency> entityType,
      String[] fieldNames, String filename) {
    this(entityType, fieldNames);
    setFilename(filename);
  }

  public Class<?> getEntityType() {
    return _entityType;
  }

  protected void setFilename(String filename) {
    _filename = filename;
  }

  public String getFilename() {
    return _filename;
  }

  public List<String> getEntityFields() {
    return _entityFields;
  }

  public void open() {

  }

  public void close() {

  }
}

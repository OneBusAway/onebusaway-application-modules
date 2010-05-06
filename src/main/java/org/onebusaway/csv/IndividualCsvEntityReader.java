/**
 * 
 */
package org.onebusaway.csv;

import edu.washington.cs.rse.text.CSVListener;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndividualCsvEntityReader implements CSVListener {

  private EntityHandler _handler;

  private CsvEntityContext _context;

  private EntitySchema _schema;

  private boolean _initialized = false;

  private List<String> _fields;

  private int _line = 1;

  private boolean _verbose = false;

  public IndividualCsvEntityReader(CsvEntityContext context, EntitySchema schema,
      EntityHandler handler) {
    _handler = handler;
    _context = context;
    _schema = schema;
  }

  public IndividualCsvEntityReader(EntityHandler handler, CsvEntityContext context,
      EntitySchema schema, List<String> fields) {
    this(context, schema, handler);
    _initialized = true;
    _fields = fields;
  }

  public void setVerbose(boolean verbose) {
    _verbose = verbose;
  }

  public void handleLine(List<String> line) throws Exception {
    if (!_initialized) {
      readSchema(line);
      _initialized = true;
    } else {
      readEntity(line);
    }
    _line++;
    if (_verbose && _line % 1000 == 0)
      System.out.println("entities=" + _line);
  }

  private void readSchema(List<String> line) {
    _fields = line;
  }

  private void readEntity(List<String> line) {

    if (line.size() != _fields.size())
      throw new IllegalStateException("line value count mismatch: line#="
          + _line + " values=" + line + " (" + line.size() + ") fields=("
          + _fields.size() + ")");

    Object object = createNewEntityInstance();
    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);

    Map<String, Object> values = new HashMap<String, Object>();

    for (int i = 0; i < line.size(); i++) {
      String csvFieldName = _fields.get(i);
      String value = line.get(i);
      values.put(csvFieldName, value);
    }

    for (FieldMapping mapping : _schema.getFields())
      mapping.translateFromCSVToObject(_context, values, wrapper);

    _handler.handleEntity(object);
  }

  private Object createNewEntityInstance() {
    Class<?> entityClass = _schema.getEntityClass();
    try {
      return entityClass.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("could not instantiate entity class "
          + entityClass, ex);
    }
  }
}
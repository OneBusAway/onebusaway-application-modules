package org.onebusaway.api.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.rest.handler.ContentTypeHandler;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.api.model.transit.ListWithReferencesBean;
import org.onebusaway.gtfs.csv.CsvEntityWriterFactory;
import org.onebusaway.gtfs.csv.EntityHandler;

public class CustomCsvHandler implements ContentTypeHandler {

  @Override
  public void toObject(Reader in, Object target) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String fromObject(Object obj, String resultCode, Writer stream)
      throws IOException {
    CsvEntityWriterFactory factory = new CsvEntityWriterFactory();
    Class<?> entityType = getEntityType(obj);
    EntityHandler csvHandler = factory.createWriter(entityType, stream);

    List<?> values = getEntityValues(obj);
    for (Object value : values)
      csvHandler.handleEntity(value);

    return null;
  }

  @Override
  public String getContentType() {
    return "text/plain";
  }

  @Override
  public String getExtension() {
    return "csv";
  }

  /****
   * 
   ****/

  private Class<?> getEntityType(Object obj) {
    if (obj instanceof ResponseBean) {
      ResponseBean response = (ResponseBean) obj;
      if (response.getData() == null)
        return response.getClass();
      return getEntityType(response.getData());
    } else if (obj instanceof EntryWithReferencesBean) {
      EntryWithReferencesBean<?> entry = (EntryWithReferencesBean<?>) obj;
      return entry.getEntry().getClass();
    } else if (obj instanceof ListWithReferencesBean) {
      ListWithReferencesBean<?> list = (ListWithReferencesBean<?>) obj;
      List<?> values = list.getList();
      if (values.isEmpty())
        return Object.class;
      return values.get(0).getClass();
    }
    return obj.getClass();
  }

  @SuppressWarnings("unchecked")
  private List<?> getEntityValues(Object obj) {
    if (obj instanceof ResponseBean) {
      ResponseBean response = (ResponseBean) obj;
      if (response.getData() == null)
        return Arrays.asList(response);
      return getEntityValues(response.getData());
    } else if (obj instanceof EntryWithReferencesBean) {
      EntryWithReferencesBean<?> entry = (EntryWithReferencesBean<?>) obj;
      return Arrays.asList(entry.getEntry());
    } else if (obj instanceof ListWithReferencesBean) {
      ListWithReferencesBean<?> list = (ListWithReferencesBean<?>) obj;
      return list.getList();
    }
    return Arrays.asList(obj);
  }
}

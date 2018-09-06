/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.api.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.rest.handler.ContentTypeHandler;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.api.model.transit.ListWithReferencesBean;
import org.onebusaway.csv_entities.CsvEntityWriterFactory;
import org.onebusaway.csv_entities.EntityHandler;

public class CustomCsvHandler implements ContentTypeHandler {

  @Override
  public void toObject(Reader in, Object target) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void toObject(ActionInvocation actionInvocation, Reader reader, Object o) throws IOException {
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
  public String fromObject(ActionInvocation actionInvocation, Object obj, String s, Writer stream) throws IOException {
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

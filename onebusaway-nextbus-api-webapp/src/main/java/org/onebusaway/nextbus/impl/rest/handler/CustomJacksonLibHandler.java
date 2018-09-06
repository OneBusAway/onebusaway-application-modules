/**
 * Copyright (C) 2015 Cambridge Systematics
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

package org.onebusaway.nextbus.impl.rest.handler;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

import org.apache.struts2.StrutsConstants;
import org.apache.struts2.rest.handler.ContentTypeHandler;
import org.onebusaway.nextbus.model.nextbus.Body;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles JSON content using jackson-lib
 */
public class CustomJacksonLibHandler implements ContentTypeHandler {

  private static final String DEFAULT_CONTENT_TYPE = "application/json";
  private String defaultEncoding = "ISO-8859-1";
  private ObjectMapper mapper = new ObjectMapper();

  public void toObject(Reader in, Object target) throws IOException {

    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    ObjectReader or = mapper.readerForUpdating(target);
    or.readValue(in); // , new TypeReference<clazz>);
  }

  @Override
  public void toObject(ActionInvocation actionInvocation, Reader reader, Object o) throws IOException {
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    ObjectReader or = mapper.readerForUpdating(o);
    or.readValue(reader); // , new TypeReference<clazz>);

  }

  public String fromObject(Object obj, String resultCode, Writer stream) throws IOException {
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
        mapper.writeValue(stream, obj);
       
        return null;
    }

  @Override
  public String fromObject(ActionInvocation actionInvocation, Object o, String s, Writer writer) throws IOException {
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
    mapper.writeValue(writer, o);

    return null;
  }

  public String getContentType() {
    return DEFAULT_CONTENT_TYPE + ";charset=" + this.defaultEncoding;
  }

  public String getExtension() {
    return "json";
  }

  @Inject(StrutsConstants.STRUTS_I18N_ENCODING)
  public void setDefaultEncoding(String val) {
    this.defaultEncoding = val;
  }

  private class ReplaceNamingStrategy extends PropertyNamingStrategy {

    private static final long serialVersionUID = 1L;

    private Map<String, String> replaceMap;

    public ReplaceNamingStrategy(Map<String, String> replaceMap) {
      this.replaceMap = replaceMap;
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config,
        AnnotatedMethod method, String defaultName) {
      if (replaceMap.containsKey(defaultName)) {
        return replaceMap.get(defaultName);
      }

      return super.nameForGetterMethod(config, method, defaultName);
    }
  }
}

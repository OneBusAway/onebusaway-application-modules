/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LowerCaseWDashesGsonJsonTool implements JsonTool{
  
  /**
   * Initialize, and specify whether we should pretty print.
   * @param prettyPrintOutput to pretty print, or not to pretty print.
   */
  public LowerCaseWDashesGsonJsonTool(boolean prettyPrintOutput) {
    super();
    
    this.prettyPrintOutput = prettyPrintOutput;
  }
  
  /**
   * Initialize using the default of no pretty printing.
   */
  public LowerCaseWDashesGsonJsonTool() {
    this(false);
  }
  
  private boolean prettyPrintOutput;
  
  public void setPrettyPrintOutput(boolean prettyPrintOutput) {
    this.prettyPrintOutput = prettyPrintOutput;
    
    buildGsonObject();
  }

  @Override
  public <T> T readJson(Reader reader, Class<T> classOfT) {
    Gson gson = buildGsonObject();
    return gson.fromJson(reader, classOfT);
  }

  @Override
  public void writeJson(Writer writer, Object objectToWrite) throws IOException {
    Gson gson = buildGsonObject();
    
    String serializedObject = gson.toJson(objectToWrite);
    
    writer.write(serializedObject);
  }
  
  private Gson buildGsonObject() {
    GsonBuilder gbuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
    
    setTypeAdapters(gbuilder);
    
    if (prettyPrintOutput)
      gbuilder.setPrettyPrinting();
    
    Gson gson = gbuilder.create();
    
    return gson;
  }
  
  private void setTypeAdapters(GsonBuilder gsonBuilder) {
    // First set Joda DateTime Adapter
    gsonBuilder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
    gsonBuilder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
  }
}

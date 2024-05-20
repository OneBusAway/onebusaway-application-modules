/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

public class JsonUtil {
  public String serialize(Object object) throws IOException {
    //serialize the status object and send to client -- it contains an id for querying
    final StringWriter sw = new StringWriter();
    final MappingJsonFactory jsonFactory = new MappingJsonFactory();
    final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(jsonGenerator, object);
    return sw.toString();
  }
}

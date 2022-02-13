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

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.onebusaway.util.OneBusAwayFormats;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JodaDateTimeAdapter implements JsonSerializer<DateTime>,
JsonDeserializer<DateTime>{

  @Override
  public DateTime deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    
    DateTimeFormatter fmt = OneBusAwayFormats.DATETIMEPATTERN_JSON_DATE_TIME;
    DateTime result = fmt.parseDateTime(json.getAsJsonPrimitive().getAsString());
    
    return result;
  }

  @Override
  public JsonElement serialize(DateTime src, Type typeOfSrc,
      JsonSerializationContext context) {
    DateTimeFormatter fmt = OneBusAwayFormats.DATETIMEPATTERN_JSON_DATE_TIME;
    return new JsonPrimitive(fmt.print(src));
  }

}

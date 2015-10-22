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
package org.onebusaway.nextbus.impl.rest.jackson;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CapitalizeSerializer extends JsonSerializer<String>{

  @Override
  public void serialize(String value, JsonGenerator gen,
      SerializerProvider serializers) throws IOException,
      JsonProcessingException {
    if(value == null){
      gen.writeNull();
    }
    gen.writeString(WordUtils.capitalizeFully(value));
  }

}

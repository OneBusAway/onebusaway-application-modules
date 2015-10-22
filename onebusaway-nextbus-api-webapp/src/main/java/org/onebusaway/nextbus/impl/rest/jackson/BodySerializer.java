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
import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.nextbus.LastTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BodySerializer<T> extends JsonSerializer<Body<T>> {

  @Override
  public void serialize(Body<T> value, JsonGenerator gen,
      SerializerProvider serializers) throws IOException,
      JsonProcessingException {

    List<T> response = value.getResponse();
    List<BodyError> errors = value.getErrors();
    String copyright = value.getCopyright();
    LastTime lastTime = value.getLastTime();

    gen.writeStartObject();
    
    // Copyright
    if (StringUtils.isNotBlank(copyright))
      gen.writeStringField("copyright", copyright);
    
    // Error
    if (errors != null && errors.size() > 0){
      for(BodyError error : errors)
        error.setContent(error.getContent().replace("\\",""));
      gen.writeObjectField("Error", errors);
    }

    // Response
    if (response != null && response.size() > 0) {
      JsonRootName jsonRootName = response.get(0).getClass().getAnnotation(
          JsonRootName.class);
      if (jsonRootName != null)
        gen.writeObjectField(jsonRootName.value(), response);
      else
        gen.writeObjectField("response", response);
    }
    
    // LastTime
    if (lastTime != null)
      gen.writeObjectField("lastTime", lastTime);

    gen.writeEndObject();
  }

}

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

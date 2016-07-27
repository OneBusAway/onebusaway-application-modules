/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.siri;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.BeanSerializer;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.map.ser.std.BeanSerializerBase;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.util.ReflectionUtils;

import uk.org.siri.siri_2.Siri;

/** 
 * Serializer for XSD-generated SIRI classes, creating JSON in the format suitable
 * for Bus Time front-ends and third-party apps.
 * 
 * @author jmaki
 *
 */
public class SiriJsonSerializerV2 {

  private static class CustomValueObjectSerializer extends BeanSerializerBase {

    private String fieldName = null;
    
    protected CustomValueObjectSerializer(BeanSerializer src, String fieldName) {
      super(src);
      this.fieldName = fieldName;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen,
        SerializerProvider provider) throws IOException, JsonGenerationException {
      
      try {
        Class<? extends Object> beanClass = bean.getClass();
        Field valueField = ReflectionUtils.findField(beanClass, fieldName);
        valueField.setAccessible(true);

        Object value = valueField.get(bean);
  
        provider.defaultSerializeValue(value, jgen);
      } catch(Exception e) {
        jgen.writeNull();
      }
    }
    
  }

  private static class CustomBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
        BasicBeanDescription beanDesc, JsonSerializer<?> serializer) {
      
      if(serializer instanceof BeanSerializer) {
        List<BeanPropertyDefinition> properties = beanDesc.findProperties();
        for(BeanPropertyDefinition property : properties) {
          if(property.getName().equals("value") || property.getName().equals("any")) {
            String fieldName = property.getField().getName();
            if(fieldName != null)
              return super.modifySerializer(config, beanDesc, new CustomValueObjectSerializer((BeanSerializer)serializer, fieldName));
          }
        }
        
      }
      
      return super.modifySerializer(config, beanDesc, serializer);
    }
  }
  
  private static class JacksonModule extends Module {
    private final static Version VERSION = new Version(1,0,0, null);
    
    @Override
    public String getModuleName() {
      return "CustomSerializer";
    }

    @Override
    public Version version() {
      return VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
      context.addBeanSerializerModifier(new CustomBeanSerializerModifier());
    }
  }

  private static class RFC822SimpleDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = 1L;

    public RFC822SimpleDateFormat() {
      super("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }
    
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer sb = super.format(date, toAppendTo, pos);
      sb.insert(sb.length() - 2, ":");
      return sb;
    }
  }

  public String getJson(Siri siri) throws Exception {    
    return getJson(siri, null);
  }

  public String getJson(Siri siri, String callback) throws Exception {    
    ObjectMapper mapper = new ObjectMapper();    
    mapper.setSerializationInclusion(Inclusion.NON_NULL);
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
    mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);   
    
    mapper.setDateFormat(new RFC822SimpleDateFormat());

    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    SerializationConfig config = mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
    mapper.setSerializationConfig(config);

    mapper.registerModule(new JacksonModule());

    String output = "";

    if(callback != null)
      output = callback + "(";

    output += mapper.writeValueAsString(siri);

    if(callback != null)
      output += ")";

    return output;
  }  

}

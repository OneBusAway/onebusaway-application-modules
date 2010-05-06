package org.onebusaway.gtdf.serialization;

import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.DefaultFieldMapping;
import org.onebusaway.csv.EntitySchemaFactory;
import org.onebusaway.csv.FieldMapping;
import org.onebusaway.csv.FieldMappingFactory;
import org.springframework.beans.BeanWrapper;

import java.util.Map;

public class AgencyIdFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      String csvFieldName, String objFieldName, Class<?> objFieldType,
      boolean required) {

    return new FieldMappingImpl(csvFieldName, objFieldName, String.class,
        required);
  }

  private class FieldMappingImpl extends DefaultFieldMapping {

    public FieldMappingImpl(String csvFieldName, String objFieldName,
        Class<?> objFieldType, boolean required) {
      super(csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      boolean missing = isMissing(csvValues);

      GTDFReaderContext ctx = (GTDFReaderContext) context.get(GTDFReader.KEY_CONTEXT);

      if (missing)
        csvValues.put(_csvFieldName, ctx.getFeedId());

      super.translateFromCSVToObject(context, csvValues, object);
    }
  }
}

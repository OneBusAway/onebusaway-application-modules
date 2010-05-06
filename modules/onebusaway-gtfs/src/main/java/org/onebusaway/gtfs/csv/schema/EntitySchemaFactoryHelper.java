package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.gtfs.csv.schema.beans.CsvFieldMappingBean;

import java.lang.reflect.Field;

public class EntitySchemaFactoryHelper {

  private DefaultEntitySchemaFactory _factory;

  public EntitySchemaFactoryHelper(DefaultEntitySchemaFactory factory) {
    _factory = factory;
  }

  public CsvEntityMappingBean addEntity(Class<?> entityClass) {
    CsvEntityMappingBean bean = new CsvEntityMappingBean(entityClass);
    _factory.addBean(bean);
    return bean;
  }

  public CsvEntityMappingBean addEntity(Class<?> entityClass, String filename) {
    CsvEntityMappingBean bean = addEntity(entityClass);
    bean.setFilename(filename);
    return bean;
  }

  public CsvEntityMappingBean addEntity(Class<?> entityClass, String filename, String prefix) {
    CsvEntityMappingBean bean = addEntity(entityClass, filename);
    bean.setPrefix(prefix);
    return bean;
  }

  public CsvFieldMappingBean addField(CsvEntityMappingBean entityBean, String fieldName) {
    Class<?> entityClass = entityBean.getType();
    try {
      Field field = entityClass.getDeclaredField(fieldName);
      CsvFieldMappingBean fieldBean = new CsvFieldMappingBean(field);
      entityBean.addField(fieldBean);
      return fieldBean;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
  
  public CsvFieldMappingBean[] addFields(CsvEntityMappingBean entityBean, String... fieldNames){
    CsvFieldMappingBean[] fields = new CsvFieldMappingBean[fieldNames.length];
    for( int i=0; i<fields.length; i++)
      fields[i] = addField(entityBean,fieldNames[i]);
    return fields;
  }

  public CsvFieldMappingBean addField(CsvEntityMappingBean entityBean, String fieldName, String csvFieldName) {
    CsvFieldMappingBean fieldBean = addField(entityBean, fieldName);
    fieldBean.setName(csvFieldName);
    return fieldBean;
  }

  public CsvFieldMappingBean addField(CsvEntityMappingBean entityBean, String fieldName, FieldMappingFactory factory) {
    CsvFieldMappingBean fieldBean = addField(entityBean, fieldName);
    fieldBean.setMapping(factory);
    return fieldBean;
  }

  public CsvFieldMappingBean addField(CsvEntityMappingBean entityBean, String fieldName, FieldMappingFactory factory,
      int order) {
    CsvFieldMappingBean fieldBean = addField(entityBean, fieldName, factory);
    fieldBean.setOrder(order);
    return fieldBean;
  }

  public CsvFieldMappingBean addField(CsvEntityMappingBean entityBean, String fieldName, String csvFieldName,
      FieldMappingFactory factory) {
    CsvFieldMappingBean fieldBean = addField(entityBean, fieldName, csvFieldName);
    fieldBean.setMapping(factory);
    return fieldBean;
  }

  public CsvFieldMappingBean addField(CsvEntityMappingBean entityBean, String fieldName, String csvFieldName,
      FieldMappingFactory factory, int order) {
    CsvFieldMappingBean fieldBean = addField(entityBean, fieldName, csvFieldName, factory);
    fieldBean.setOrder(order);
    return fieldBean;
  }

  public CsvFieldMappingBean addOptionalField(CsvEntityMappingBean entityBean, String fieldName) {
    CsvFieldMappingBean fieldBean = addField(entityBean, fieldName);
    fieldBean.setOptional(true);
    return fieldBean;
  }

  public CsvFieldMappingBean addOptionalField(CsvEntityMappingBean entityBean, String fieldName, String csvFieldName) {
    CsvFieldMappingBean fieldBean = addOptionalField(entityBean, fieldName);
    fieldBean.setName(csvFieldName);
    return fieldBean;
  }

  public CsvFieldMappingBean addOptionalField(CsvEntityMappingBean entityBean, String fieldName,
      FieldMappingFactory fieldMappingFactory) {
    CsvFieldMappingBean fieldBean = addOptionalField(entityBean, fieldName);
    fieldBean.setMapping(fieldMappingFactory);
    return fieldBean;
  }

  public CsvFieldMappingBean addOptionalField(CsvEntityMappingBean entityBean, String fieldName, String csvFieldName,
      FieldMappingFactory fieldMappingFactory) {
    CsvFieldMappingBean fieldBean = addOptionalField(entityBean, fieldName, csvFieldName);
    fieldBean.setMapping(fieldMappingFactory);
    return fieldBean;
  }

  public CsvFieldMappingBean[] addOptionalFields(CsvEntityMappingBean entityBean, String... fieldNames) {
    CsvFieldMappingBean[] beans = new CsvFieldMappingBean[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      beans[i] = addField(entityBean, fieldNames[i]);
      beans[i].setOptional(true);
    }
    return beans;
  }

  public CsvFieldMappingBean addIgnorableField(CsvEntityMappingBean entityBean, String fieldName) {
    CsvFieldMappingBean bean = addField(entityBean, fieldName);
    bean.setIgnore(true);
    return bean;
  }
}

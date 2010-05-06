package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.gtfs.csv.schema.beans.CsvFieldMappingBean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEntitySchemaFactoryImpl implements EntitySchemaFactory, ListableCsvMappingFactory {

  private boolean _initialized = false;

  private Map<Class<?>, CsvEntityMappingBean> _mappingBeansByClass = new HashMap<Class<?>, CsvEntityMappingBean>();

  private Map<Class<?>, EntitySchema> _schemasByClass = new HashMap<Class<?>, EntitySchema>();

  /****
   * {@link ListableCsvMappingFactory} Interface
   ****/

  public Collection<CsvEntityMappingBean> getEntityMappings() {
    initialize();
    return new ArrayList<CsvEntityMappingBean>(_mappingBeansByClass.values());
  }

  /****
   * {@link EntitySchemaFactory} Interface
   ****/

  public EntitySchema getSchema(Class<?> entityClass) {

    initialize();

    EntitySchema schema = _schemasByClass.get(entityClass);

    if (schema == null) {
      schema = createSchemaForEntityClass(entityClass);
      _schemasByClass.put(entityClass, schema);
    }

    return schema;
  }

  /****
   * Protected Methods
   ****/

  protected abstract void processBeanDefinitions();

  protected void registerBeanDefinition(CsvEntityMappingBean bean) {
    CsvEntityMappingBean existingBean = _mappingBeansByClass.get(bean.getType());
    if (existingBean != null) {
      CsvEntityMappingBean merged = new CsvEntityMappingBean(bean.getType());
      mergeBeans(existingBean, merged);
      mergeBeans(bean, merged);
      bean = merged;
    }
    _mappingBeansByClass.put(bean.getType(), bean);
  }

  protected void applyCsvFieldsAnnotationToBean(Class<?> entityClass, CsvEntityMappingBean entityBean) {

    CsvFields csvFields = entityClass.getAnnotation(CsvFields.class);

    if (csvFields != null) {
      entityBean.setFilename(csvFields.filename());
      if (!csvFields.prefix().equals(""))
        entityBean.setPrefix(csvFields.prefix());
      if (csvFields.required())
        entityBean.setRequired(csvFields.required());
    }
  }

  protected void applyCsvFieldAnnotationToBean(Field field, CsvFieldMappingBean fieldBean) {
    CsvField csvField = field.getAnnotation(CsvField.class);

    if (csvField != null) {
      if (!csvField.name().equals(""))
        fieldBean.setName(csvField.name());
      if (csvField.ignore())
        fieldBean.setIgnore(csvField.ignore());
      if (csvField.optional())
        fieldBean.setOptional(csvField.optional());
      if (csvField.order() != 0)
        fieldBean.setOrder(csvField.order());

      Class<? extends FieldMappingFactory> mapping = csvField.mapping();
      if (!mapping.equals(FieldMappingFactory.class)) {
        try {
          FieldMappingFactory factory = mapping.newInstance();
          fieldBean.setMapping(factory);
        } catch (Exception ex) {
          throw new IllegalStateException("error creating field mapping factory of type " + mapping.getName(), ex);
        }
      }
    }
  }

  /****
   * Private Methods
   ****/

  private void initialize() {
    if (!_initialized) {
      processBeanDefinitions();
      _initialized = true;
    }
  }

  private void mergeBeans(CsvEntityMappingBean source, CsvEntityMappingBean target) {
    if (source.isFilenameSet())
      target.setFilename(source.getFilename());
    if (source.isPrefixSet())
      target.setPrefix(source.getPrefix());
    if (source.isRequiredSet())
      target.setRequired(source.isRequired());
    Map<Field, CsvFieldMappingBean> sourceFields = source.getFields();
    Map<Field, CsvFieldMappingBean> targetFields = target.getFields();
    for (Map.Entry<Field, CsvFieldMappingBean> entry : sourceFields.entrySet()) {
      Field sourceField = entry.getKey();
      CsvFieldMappingBean sourceFieldBean = entry.getValue();
      CsvFieldMappingBean targetFieldBean = targetFields.get(sourceField);
      if (targetFieldBean == null)
        targetFieldBean = sourceFieldBean;
      else
        mergeFields(sourceFieldBean, targetFieldBean);
      targetFields.put(sourceField, targetFieldBean);
    }
  }

  private void mergeFields(CsvFieldMappingBean source, CsvFieldMappingBean target) {
    if (source.isNameSet())
      target.setName(source.getName());
    if (source.isIgnoreSet())
      target.setIgnore(target.isIgnore());
    if (source.isMappingSet())
      target.setMapping(source.getMapping());
    if (source.isOptionalSet())
      target.setOptional(source.isOptional());
    if (source.isOrderSet())
      target.setOrder(source.getOrder());
  }

  private EntitySchema createSchemaForEntityClass(Class<?> entityClass) {

    CsvEntityMappingBean mappingBean = _mappingBeansByClass.get(entityClass);

    if (mappingBean == null) {
      mappingBean = new CsvEntityMappingBean(entityClass);
      applyCsvFieldsAnnotationToBean(entityClass, mappingBean);
    }

    String name = getEntityClassAsEntityName(entityClass);
    if (mappingBean.isFilenameSet())
      name = mappingBean.getFilename();

    String prefix = "";
    if (mappingBean.isPrefixSet())
      prefix = mappingBean.getPrefix();

    boolean required = false;
    if (mappingBean.isRequiredSet())
      required = mappingBean.isRequired();

    EntitySchema schema = new EntitySchema(entityClass, name, required);

    Map<Field, CsvFieldMappingBean> existingFieldBeans = mappingBean.getFields();

    List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();

    for (Field field : entityClass.getDeclaredFields()) {

      CsvFieldMappingBean fieldMappingBean = existingFieldBeans.get(field);
      if (fieldMappingBean == null) {
        fieldMappingBean = new CsvFieldMappingBean(field);
        applyCsvFieldAnnotationToBean(field, fieldMappingBean);

        // Ignore static or final fields
        boolean ignore = (field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0;
        fieldMappingBean.setIgnore(ignore);
      }

      if (fieldMappingBean.isIgnoreSet() && fieldMappingBean.isIgnore())
        continue;

      FieldMapping mapping = getFieldMapping(field, fieldMappingBean, prefix);
      fieldMappings.add(mapping);
    }

    Collections.sort(fieldMappings, new FieldMappingComparator());

    for (FieldMapping mapping : fieldMappings)
      schema.addField(mapping);

    List<EntityValidator> validators = new ArrayList<EntityValidator>();
    validators.addAll(mappingBean.getValidators());

    Collections.sort(validators, new ValidatorComparator());

    for (EntityValidator validator : validators)
      schema.addValidator(validator);

    return schema;
  }

  private FieldMapping getFieldMapping(Field field, CsvFieldMappingBean fieldMappingBean, String prefix) {

    FieldMapping mapping = null;

    String objFieldName = field.getName();
    Class<?> objFieldType = field.getType();

    String csvFieldName = prefix + getObjectFieldNameAsCSVFieldName(objFieldName);
    boolean required = true;

    if (fieldMappingBean.isOptionalSet())
      required = !fieldMappingBean.isOptional();

    if (fieldMappingBean.isNameSet())
      csvFieldName = fieldMappingBean.getName();

    if (fieldMappingBean.isMappingSet()) {
      FieldMappingFactory factory = fieldMappingBean.getMapping();
      mapping = factory.createFieldMapping(this, csvFieldName, objFieldName, objFieldType, required);
    }

    if (mapping == null)
      mapping = new DefaultFieldMapping(csvFieldName, objFieldName, objFieldType, required);

    if (fieldMappingBean.isOptionalSet())
      mapping.setOrder(fieldMappingBean.getOrder());

    return mapping;
  }

  private String getEntityClassAsEntityName(Class<?> entityClass) {
    String name = entityClass.getName();
    int index = name.lastIndexOf(".");
    if (index != -1)
      name = name.substring(index + 1);
    return name;
  }

  private String getObjectFieldNameAsCSVFieldName(String fieldName) {

    StringBuilder b = new StringBuilder();
    boolean wasUpperCase = false;

    for (int i = 0; i < fieldName.length(); i++) {
      char c = fieldName.charAt(i);
      boolean isUpperCase = Character.isUpperCase(c);
      if (isUpperCase)
        c = Character.toLowerCase(c);
      if (isUpperCase && !wasUpperCase)
        b.append('_');
      b.append(c);
      wasUpperCase = isUpperCase;
    }

    return b.toString();
  }

  private static class FieldMappingComparator implements Comparator<FieldMapping> {
    public int compare(FieldMapping o1, FieldMapping o2) {
      return o1.getOrder() - o2.getOrder();
    }
  }

  private static class ValidatorComparator implements Comparator<EntityValidator> {
    public int compare(EntityValidator o1, EntityValidator o2) {
      return o1.getOrder() - o2.getOrder();
    }
  }
}
